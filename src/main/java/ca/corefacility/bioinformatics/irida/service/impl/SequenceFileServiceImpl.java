package ca.corefacility.bioinformatics.irida.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.corefacility.bioinformatics.irida.exceptions.EntityNotFoundException;
import ca.corefacility.bioinformatics.irida.exceptions.FileProcessorTimeoutException;
import ca.corefacility.bioinformatics.irida.exceptions.InvalidPropertyException;
import ca.corefacility.bioinformatics.irida.model.SequenceFile;
import ca.corefacility.bioinformatics.irida.model.joins.Join;
import ca.corefacility.bioinformatics.irida.model.run.SequencingRun;
import ca.corefacility.bioinformatics.irida.model.sample.Sample;
import ca.corefacility.bioinformatics.irida.model.sample.SampleSequenceFileJoin;
import ca.corefacility.bioinformatics.irida.processing.FileProcessingChain;
import ca.corefacility.bioinformatics.irida.repositories.SequenceFileRepository;
import ca.corefacility.bioinformatics.irida.repositories.joins.sample.SampleSequenceFileJoinRepository;
import ca.corefacility.bioinformatics.irida.service.SequenceFileService;

/**
 * Implementation for managing {@link SequenceFile}.
 * 
 * @author Franklin Bristow <franklin.bristow@phac-aspc.gc.ca>
 */
@Service
public class SequenceFileServiceImpl extends CRUDServiceImpl<Long, SequenceFile> implements SequenceFileService {

	private static final Logger logger = LoggerFactory.getLogger(SequenceFileServiceImpl.class);

	/**
	 * Reference to {@link SampleSequenceFileJoinRepository}.
	 */
	private final SampleSequenceFileJoinRepository ssfRepository;

	/**
	 * Reference to {@link SequenceFileRepository}.
	 */
	private final SequenceFileRepository sequenceFileRepository;

	/**
	 * Executor for running pipelines asynchronously.
	 */
	private final Executor executor;
	/**
	 * File processing chain to execute on sequence files.
	 */
	private final FileProcessingChain fileProcessingChain;

	protected SequenceFileServiceImpl() {
		super(null, null, SequenceFile.class);
		throw new UnsupportedOperationException();
	}

	/**
	 * Constructor.
	 * 
	 * @param sequenceFileRepository
	 *            the sequence file repository.
	 * @param validator
	 *            validator.
	 */
	@Autowired
	public SequenceFileServiceImpl(SequenceFileRepository sequenceFileRepository,
			SampleSequenceFileJoinRepository ssfRepository, Executor executor, FileProcessingChain fileProcessingChain, Validator validator) {
		super(sequenceFileRepository, validator, SequenceFile.class);
		this.sequenceFileRepository = sequenceFileRepository;
		this.ssfRepository = ssfRepository;
		this.executor = executor;
		this.fileProcessingChain = fileProcessingChain;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public SequenceFile create(SequenceFile sequenceFile) {
		// Send the file to the database repository to be stored (in super)
		logger.trace("Calling super.create");
		SequenceFile sf = super.create(sequenceFile);
		executor.execute(new SequenceFileProcessorLauncher(fileProcessingChain, sf, SecurityContextHolder.getContext()));
		return sf;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SequenceFile read(Long id) throws EntityNotFoundException {
		return super.read(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public SequenceFile update(Long id, Map<String, Object> updatedFields) throws InvalidPropertyException {
		SequenceFile sf = super.update(id, updatedFields);
		executor.execute(new SequenceFileProcessorLauncher(fileProcessingChain, sf, SecurityContextHolder.getContext()));
		return sf;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public Join<Sample, SequenceFile> createSequenceFileInSample(SequenceFile sequenceFile, Sample sample) {
		SequenceFile created = create(sequenceFile);
		executor.execute(new SequenceFileProcessorLauncher(fileProcessingChain, created, SecurityContextHolder.getContext()));
		SampleSequenceFileJoin join = new SampleSequenceFileJoin(sample, created);
		return ssfRepository.save(join);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Join<Sample, SequenceFile>> getSequenceFilesForSample(Sample sample) {
		return ssfRepository.getFilesForSample(sample);
	}

	@Override
	@Transactional(readOnly = true)
	public Set<SequenceFile> getSequenceFilesForSequencingRun(SequencingRun miseqRun) {
		return sequenceFileRepository.findSequenceFilesForSequencingRun(miseqRun);
	}

	/**
	 * Executes {@link FileProcessingChain} asynchronously in a
	 * {@link TaskExecutor}.
	 * 
	 * @author Franklin Bristow <franklin.bristow@phac-aspc.gc.ca>
	 * 
	 */
	private static final class SequenceFileProcessorLauncher implements Runnable {
		private final FileProcessingChain fileProcessingChain;
		private final SequenceFile sequenceFile;
		private final SecurityContext securityContext;

		public SequenceFileProcessorLauncher(FileProcessingChain fileProcessingChain, SequenceFile sequenceFile,
				SecurityContext securityContext) {
			this.fileProcessingChain = fileProcessingChain;
			this.sequenceFile = sequenceFile;
			this.securityContext = securityContext;
		}

		@Override
		public void run() {
			// when running in single-threaded mode, the security context should
			// already be populated in the current thread and and we shouldn't
			// have to overwrite and erase the context before execution.
			boolean copiedSecurityContext = true;
			SecurityContext context = SecurityContextHolder.getContext();
			if (context == null || context.getAuthentication() == null) {
				SecurityContextHolder.setContext(securityContext);
			} else {
				copiedSecurityContext = false;
			}

			// proceed with analysis
			try {
				fileProcessingChain.launchChain(sequenceFile);
			} catch (FileProcessorTimeoutException e) {
				logger.error("FileProcessingChain did *not* execute -- the transaction opened by SequenceFileService never closed.");
			}

			// erase the security context if we copied the context into the
			// current thread.
			if (copiedSecurityContext) {
				SecurityContextHolder.clearContext();
			}
		}
	}
}
