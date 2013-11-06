package ca.corefacility.bioinformatics.irida.service.impl.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import ca.corefacility.bioinformatics.irida.config.IridaApiServicesConfig;
import ca.corefacility.bioinformatics.irida.config.data.IridaApiTestDataSourceConfig;
import ca.corefacility.bioinformatics.irida.config.data.jpa.TestJpaProperties;
import ca.corefacility.bioinformatics.irida.exceptions.EntityExistsException;
import ca.corefacility.bioinformatics.irida.model.MiseqRun;
import ca.corefacility.bioinformatics.irida.model.Role;
import ca.corefacility.bioinformatics.irida.model.SequenceFile;
import ca.corefacility.bioinformatics.irida.model.User;
import ca.corefacility.bioinformatics.irida.model.joins.Join;
import ca.corefacility.bioinformatics.irida.service.MiseqRunService;
import ca.corefacility.bioinformatics.irida.service.SequenceFileService;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.google.common.collect.ImmutableList;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = { IridaApiServicesConfig.class,
		IridaApiTestDataSourceConfig.class, TestJpaProperties.class })
@ActiveProfiles("test")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class MiseqServiceImplIT {
	@Autowired
	private MiseqRunService miseqRunService;
	@Autowired
	private SequenceFileService sequenceFileService;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	@DatabaseSetup("/ca/corefacility/bioinformatics/irida/service/impl/MiseqServiceImplIT.xml")
	@DatabaseTearDown("/ca/corefacility/bioinformatics/irida/service/impl/MiseqServiceImplIT.xml")
	public void testAddSequenceFileToMiseqRunAsAdmin() {
		SequenceFile sf = asRole(Role.ROLE_ADMIN).sequenceFileService.read(1l);
		MiseqRun miseqRun = asRole(Role.ROLE_ADMIN).miseqRunService.read(1l);

		try {
			Join<MiseqRun, SequenceFile> j = asRole(Role.ROLE_ADMIN).miseqRunService.addSequenceFileToMiseqRun(
					miseqRun, sf);
			assertNotNull("Join was empty.", j);
			assertEquals("Join had wrong sequence file.", sf, j.getObject());
			assertEquals("Join had wrong miseq run.", miseqRun, j.getSubject());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Test failed for unknown reason.");
		}
	}

	@Test(expected = AccessDeniedException.class)
	@DatabaseSetup("/ca/corefacility/bioinformatics/irida/service/impl/MiseqServiceImplIT.xml")
	@DatabaseTearDown("/ca/corefacility/bioinformatics/irida/service/impl/MiseqServiceImplIT.xml")
	public void testAddSequenceFileToMiseqRunAsManager() {
		SequenceFile sf = asRole(Role.ROLE_MANAGER).sequenceFileService.read(1l);
		MiseqRun miseqRun = asRole(Role.ROLE_MANAGER).miseqRunService.read(1l);
		asRole(Role.ROLE_MANAGER).miseqRunService.addSequenceFileToMiseqRun(miseqRun, sf);
	}

	@Test(expected = AccessDeniedException.class)
	@DatabaseSetup("/ca/corefacility/bioinformatics/irida/service/impl/MiseqServiceImplIT.xml")
	@DatabaseTearDown("/ca/corefacility/bioinformatics/irida/service/impl/MiseqServiceImplIT.xml")
	public void testAddSequenceFileToMiseqRunAsUser() {
		SequenceFile sf = asRole(Role.ROLE_USER).sequenceFileService.read(1l);
		MiseqRun miseqRun = asRole(Role.ROLE_USER).miseqRunService.read(1l);
		asRole(Role.ROLE_USER).miseqRunService.addSequenceFileToMiseqRun(miseqRun, sf);
	}

	@Test(expected = EntityExistsException.class)
	@DatabaseSetup("/ca/corefacility/bioinformatics/irida/service/impl/MiseqServiceImplIT.xml")
	@DatabaseTearDown("/ca/corefacility/bioinformatics/irida/service/impl/MiseqServiceImplIT.xml")
	public void testAddSequenceFileToMiseqRunMultiple() {
		SequenceFile sf = asRole(Role.ROLE_ADMIN).sequenceFileService.read(1l);
		MiseqRun miseqRun = asRole(Role.ROLE_ADMIN).miseqRunService.read(1l);
		asRole(Role.ROLE_ADMIN).miseqRunService.addSequenceFileToMiseqRun(miseqRun, sf);
		asRole(Role.ROLE_ADMIN).miseqRunService.addSequenceFileToMiseqRun(miseqRun, sf);
	}

	@Test
	@DatabaseSetup("/ca/corefacility/bioinformatics/irida/service/impl/MiseqServiceImplIT.xml")
	@DatabaseTearDown("/ca/corefacility/bioinformatics/irida/service/impl/MiseqServiceImplIT.xml")
	public void testGetMiseqRunForSequenceFile() {
		SequenceFile sf = asRole(Role.ROLE_ADMIN).sequenceFileService.read(2l);

		try {
			Join<MiseqRun, SequenceFile> j = asRole(Role.ROLE_ADMIN).miseqRunService.getMiseqRunForSequenceFile(sf);
			assertNotNull("Join was empty.", j);
			assertEquals("Join had wrong sequence file.", sf, j.getObject());
			assertEquals("Join had wrong miseq run.", Long.valueOf(2l), j.getSubject().getId());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Test failed for unknown reason.");
		}
	}

	private MiseqServiceImplIT asRole(Role r) {
		User u = new User();
		u.setUsername("fbristow");
		u.setPassword(passwordEncoder.encode("Password1"));
		u.setSystemRole(r);
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(u, "Password1",
				ImmutableList.of(r));
		auth.setDetails(u);
		SecurityContextHolder.getContext().setAuthentication(auth);
		return this;
	}
}
