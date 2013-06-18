package ca.corefacility.bioinformatics.irida.service.impl;

import ca.corefacility.bioinformatics.irida.model.Project;
import ca.corefacility.bioinformatics.irida.model.Relationship;
import ca.corefacility.bioinformatics.irida.model.Sample;
import ca.corefacility.bioinformatics.irida.model.SequenceFile;
import ca.corefacility.bioinformatics.irida.model.roles.impl.Identifier;
import ca.corefacility.bioinformatics.irida.repositories.CRUDRepository;
import ca.corefacility.bioinformatics.irida.repositories.RelationshipRepository;
import ca.corefacility.bioinformatics.irida.repositories.SequenceFileRepository;
import ca.corefacility.bioinformatics.irida.repositories.sesame.dao.RdfPredicate;
import ca.corefacility.bioinformatics.irida.service.SampleService;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SampleServiceImpl}.
 *
 * @author Franklin Bristow <franklin.bristow@phac-aspc.gc.ca>
 */
public class SampleServiceImplTest {

    private SampleService sampleService;
    private CRUDRepository<Identifier, Sample> sampleRepository;
    private RelationshipRepository relationshipRepository;
    private SequenceFileRepository sequenceFileRepository;
    private Validator validator;

    @Before
    public void setUp() {
        sampleRepository = mock(CRUDRepository.class);
        relationshipRepository = mock(RelationshipRepository.class);
        sequenceFileRepository = mock(SequenceFileRepository.class);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        sampleService = new SampleServiceImpl(sampleRepository, relationshipRepository, sequenceFileRepository, validator);
    }

    @Test
    public void testGetSampleForProject() {
        Project p = new Project();
        p.setIdentifier(new Identifier());
        Sample s = new Sample();
        s.setIdentifier(new Identifier());
        Relationship r = new Relationship(p.getIdentifier(), s.getIdentifier());
        r.setIdentifier(new Identifier());
        List<Relationship> relationships = new ArrayList<>(Sets.newHashSet(r));
        when(relationshipRepository.getLinks(p.getIdentifier(), RdfPredicate.ANY, s.getIdentifier())).thenReturn(relationships);
        when(sampleRepository.read(s.getIdentifier())).thenReturn(s);

        sampleService.getSampleForProject(p, s.getIdentifier());

        verify(relationshipRepository).getLinks(p.getIdentifier(), RdfPredicate.ANY, s.getIdentifier());
        verify(sampleRepository).read(s.getIdentifier());
    }

    @Test
    public void testAddExistingSequenceFileToSample() {
        Sample s = new Sample();
        s.setIdentifier(new Identifier());
        SequenceFile sf = new SequenceFile();
        sf.setIdentifier(new Identifier());
        Relationship r = new Relationship(s.getIdentifier(), sf.getIdentifier());
        Project p = new Project();
        p.setIdentifier(new Identifier());
        Relationship projectSequenceFile = new Relationship(p.getIdentifier(), sf.getIdentifier());
        projectSequenceFile.setIdentifier(new Identifier());
        List<Relationship> relationships = Lists.newArrayList(projectSequenceFile);

        when(sampleRepository.exists(s.getIdentifier())).thenReturn(Boolean.TRUE);
        when(sequenceFileRepository.exists(sf.getIdentifier())).thenReturn(Boolean.TRUE);
        when(relationshipRepository.create(Sample.class, s.getIdentifier(), SequenceFile.class, sf.getIdentifier())).thenReturn(r);
        when(relationshipRepository.getLinks(p.getIdentifier(), RdfPredicate.ANY, sf.getIdentifier())).thenReturn(relationships);

        Relationship created = sampleService.addSequenceFileToSample(p, s, sf);

        verify(sampleRepository).exists(s.getIdentifier());
        verify(sequenceFileRepository).exists(sf.getIdentifier());
        verify(relationshipRepository).create(Sample.class, s.getIdentifier(), SequenceFile.class, sf.getIdentifier());
        verify(relationshipRepository).getLinks(p.getIdentifier(), RdfPredicate.ANY, sf.getIdentifier());
        verify(relationshipRepository).delete(projectSequenceFile.getIdentifier());

        assertEquals(r, created);
    }

    @Test
    public void testRemoveSequenceFileFromSample() {
        Sample s = new Sample();
        s.setIdentifier(new Identifier());
        SequenceFile sf = new SequenceFile();
        sf.setIdentifier(new Identifier());
        Project p = new Project();
        p.setIdentifier(new Identifier());
        Relationship r = new Relationship(p.getIdentifier(), sf.getIdentifier());
        Relationship sampleSequenceFile = new Relationship(s.getIdentifier(), sf.getIdentifier());
        sampleSequenceFile.setIdentifier(new Identifier());
        List<Relationship> relationships = new ArrayList<>();
        relationships.add(sampleSequenceFile);

        when(relationshipRepository.getLinks(p.getIdentifier(), RdfPredicate.ANY, s.getIdentifier()))
                .thenReturn(new ArrayList<Relationship>());
        when(relationshipRepository.getLinks(s.getIdentifier(), RdfPredicate.ANY, sf.getIdentifier()))
                .thenReturn(relationships);
        when(relationshipRepository.create(p, sf)).thenReturn(r);

        Relationship created = sampleService.removeSequenceFileFromSample(p, s, sf);

        verify(relationshipRepository).getLinks(p.getIdentifier(), RdfPredicate.ANY, s.getIdentifier());
        verify(relationshipRepository).getLinks(s.getIdentifier(), RdfPredicate.ANY, sf.getIdentifier());
        verify(relationshipRepository).delete(sampleSequenceFile.getIdentifier());
        verify(relationshipRepository).create(p, sf);

        assertEquals(created, r);
    }
}
