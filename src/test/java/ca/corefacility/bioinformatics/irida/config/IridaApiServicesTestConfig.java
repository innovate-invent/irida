package ca.corefacility.bioinformatics.irida.config;

import java.util.concurrent.ExecutorService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import com.google.common.util.concurrent.MoreExecutors;

import ca.corefacility.bioinformatics.irida.config.analysis.AnalysisExecutionServiceTestConfig;
import ca.corefacility.bioinformatics.irida.config.pipeline.data.galaxy.NonWindowsLocalGalaxyConfig;
import ca.corefacility.bioinformatics.irida.config.pipeline.data.galaxy.WindowsLocalGalaxyConfig;
import ca.corefacility.bioinformatics.irida.config.workflow.RemoteWorkflowServiceTestConfig;

/**
 * Config name for test services that need to be setup.
 * 
 * @author Aaron Petkau <aaron.petkau@phac-aspc.gc.ca>
 *
 */
@Configuration
@Import({ NonWindowsLocalGalaxyConfig.class, WindowsLocalGalaxyConfig.class,
		RemoteWorkflowServiceTestConfig.class,
		AnalysisExecutionServiceTestConfig.class })
@Profile("test")
public class IridaApiServicesTestConfig {
	
	/**
	 * @return An ExecutorService executing code in the same thread for testing purposes.
	 */
	@Bean
	public ExecutorService uploadExecutorService() {
		return MoreExecutors.sameThreadExecutor();
	}
}
