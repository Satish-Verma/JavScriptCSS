package com.samsung.rms.core.services.impl.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;

import com.google.common.eventbus.EventBus;
import com.samsung.rms.common.api.logging.RmsLogger;
import com.samsung.rms.common.impl.logging.LogLevel;
import com.samsung.rms.common.util.RmsUtils;
import com.samsung.rms.common.util.ScriptUtil;
import com.samsung.rms.core.services.api.app.ApplicationService;
import com.samsung.rms.core.services.api.backup.BackupService;
import com.samsung.rms.core.services.api.backup.BackupSettingsService;
import com.samsung.rms.core.services.api.cache.CacheService;
import com.samsung.rms.core.services.api.clients.ClientService;
import com.samsung.rms.core.services.api.db.DbService;
import com.samsung.rms.core.services.api.download.DownloadHandlerService;
import com.samsung.rms.core.services.api.events.UIEventService;
import com.samsung.rms.core.services.api.failover.FailoverService;
import com.samsung.rms.core.services.api.failover.RmsClusterService;
import com.samsung.rms.core.services.api.firmware.FirmwareService;
import com.samsung.rms.core.services.api.groups.GroupService;
import com.samsung.rms.core.services.api.logcollector.LogCollectorService;
import com.samsung.rms.core.services.api.member.authentication.AuthenticationService;
import com.samsung.rms.core.services.api.member.directory.DirectoryService;
import com.samsung.rms.core.services.api.metadata.MetadataService;
import com.samsung.rms.core.services.api.platform.PlatformService;
import com.samsung.rms.core.services.api.profile.ProfileService;
import com.samsung.rms.core.services.api.scheduler.SchedulerService;
import com.samsung.rms.core.services.api.search.SearchService;
import com.samsung.rms.core.services.api.security.TokenAuthenticationService;
import com.samsung.rms.core.services.api.settings.CitrixConfigureService;
import com.samsung.rms.core.services.api.settings.ConfigurationStatusService;
import com.samsung.rms.core.services.api.settings.EmailConfigurationService;
import com.samsung.rms.core.services.api.systeminfo.SystemInfoService;
import com.samsung.rms.core.services.api.task.RecentTaskService;
import com.samsung.rms.core.services.api.task.TaskService;
import com.samsung.rms.core.services.api.task.TaskStatusService;




/**
 * The Class ApplicationServiceImpl.
 */
public class ApplicationServiceImpl implements ApplicationService,
		ApplicationListener<ApplicationContextEvent> {

	private final DbService dbService_;
	private final RmsLogger logger_;
	private final SearchService searchService_;

	private final SchedulerService schedulerService_;
	private final MetadataService metadataService_;
	
	private final ClientService clientService_;
	private final GroupService groupService_;
	private final ProfileService profileService_;
	private final PlatformService platformService_;
	private final CacheService cacheService_;
	
	private final BackupService backupService_;
	private final BackupSettingsService backupSettingsService_;

	private final DirectoryService directoryService_;
	private final AuthenticationService authenticationService_;
	private final TokenAuthenticationService tokenAuthenticationService_;
	
	private final CitrixConfigureService citrixConfigureService_;	
	private final EmailConfigurationService emailConfigurationService_;
	
	private final ConfigurationStatusService configurationStatusService_;
	
	private final FirmwareService firmwareService_;

	private final TaskService taskService_;
	private final TaskStatusService taskStatusService_;
	private final RecentTaskService recentTaskService_;

	private final RmsClusterService rmsClusterService_;
	private final FailoverService failoverService_;

	private final DownloadHandlerService downloadHandlerService_;

	private final EventBus eventBus_;
	private final UIEventService uiEventService_;
	
	private final LogCollectorService logCollectorService_;
	
	private final SystemInfoService systemInfoService_;
	
	private Environment env_;
	
	private Object lock_ = new Object();
	
	/**
	 * Instantiates a new application service impl.
	 *
	 * @param dbService the db service
	 * @param clientService the client service
	 * @param groupService the group service
	 * @param profileService the profile service
	 * @param platformService the platform service
	 * @param metadataService the metadata service
	 * @param cacheService the cache service
	 * @param searchService the search service
	 * @param backupSettingsService the backup settings service
	 * @param backupService the backup service
	 * @param schedulerService the scheduler service
	 * @param directoryService the directory service
	 * @param authenticationService the authentication service
	 * @param tokenAuthenticationService the token authentication service
	 * @param citrixConfigureService the citrix configure service
	 * @param emailConfigurationService the email configuration service
	 * @param firmwareService the firmware service
	 * @param downloadChunkHandlerService the download chunk handler service
	 * @param rmsClusterService the rms cluster service
	 * @param failoverService the failover service
	 * @param eventBus the event bus
	 * @param uiEventService the ui event service
	 * @param taskService the task service
	 * @param taskStatusService the task status service
	 * @param recentTaskService the recent task service
	 * @param logCollectorService the log collector service
	 * @param configurationStatusService the configuration status service
	 * @param systemInfoService the system info service
	 * @param rmsLogger the rms logger
	 */
	@Autowired
	public ApplicationServiceImpl(
			final DbService dbService,
			final ClientService clientService,
			final GroupService groupService,
			final ProfileService profileService,
			final PlatformService platformService,
			final MetadataService metadataService,
			final CacheService cacheService, final SearchService searchService,
			final BackupSettingsService backupSettingsService,
			final BackupService backupService,
  		    final SchedulerService schedulerService,
		    final DirectoryService directoryService,
		    final AuthenticationService authenticationService,
		    final TokenAuthenticationService tokenAuthenticationService,
		    final CitrixConfigureService citrixConfigureService,
			final EmailConfigurationService emailConfigurationService,
		    final FirmwareService firmwareService,
		    final DownloadHandlerService downloadChunkHandlerService,
			final RmsClusterService rmsClusterService, final FailoverService failoverService, final EventBus eventBus,
			final UIEventService uiEventService, final TaskService taskService,			
			final TaskStatusService taskStatusService,
			final RecentTaskService recentTaskService,
			final LogCollectorService logCollectorService,			
			final ConfigurationStatusService configurationStatusService, 
			final SystemInfoService systemInfoService,
			final RmsLogger rmsLogger) {
		this.dbService_ = dbService;
		this.clientService_ = clientService;
		this.groupService_ = groupService;
		this.profileService_ = profileService;
		this.platformService_ = platformService;
		this.metadataService_ = metadataService;
		this.cacheService_ = cacheService;
		this.searchService_ = searchService;
		this.logger_ = rmsLogger;
		this.backupSettingsService_ = backupSettingsService;
		this.backupService_ = backupService;
		this.schedulerService_ = schedulerService;
		this.directoryService_ = directoryService;
		this.authenticationService_ = authenticationService;
		this.tokenAuthenticationService_ = tokenAuthenticationService;
		this.citrixConfigureService_ = citrixConfigureService;
		this.emailConfigurationService_ = emailConfigurationService;
		this.firmwareService_ = firmwareService;
		this.rmsClusterService_ = rmsClusterService;
		this.failoverService_ = failoverService;
		this.eventBus_ = eventBus;
		this.uiEventService_ = uiEventService;
		this.downloadHandlerService_ = downloadChunkHandlerService;
		this.taskService_ = taskService;
		this.taskStatusService_ = taskStatusService;
		this.recentTaskService_ = recentTaskService;
		this.logCollectorService_ = logCollectorService;
		this.configurationStatusService_ = configurationStatusService;
		this.systemInfoService_ = systemInfoService;
	}
	
	// Note: We are using setter injection because mockito is not able to inject on the field. 
	// Calling this setter explicitly from unit test method in ApplicationServiceTest
	// We should be passing environment object to constructor as well, then mockito will work.
	@Autowired
	public void setEnvironment(Environment env) {
		this.env_ = env;
	}
	
	// Note: One way to start the application would be to use PostConstruct in
	// ApplicationService
	// as this contains autowired refs to all the beans. But instead
	// ApplicationEvent Listener
	// is a better strategy as it confirms that all the beans in the context are
	// fully inited.
	// Spring does not guarranty (nor recommend) a way to make sure the order in
	// which Beans are inited.
	// To rely on PostConstruct of ApplicationService is not definitive in that
	// case, as some other
	// root-context beans that are not referred by ApplicationService might not
	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
	 */
	// have been inited yet.
	@Override
	public void onApplicationEvent(ApplicationContextEvent event) {
		try {
			if (isRootContext(event.getApplicationContext())) {
				if (event instanceof ContextRefreshedEvent) {
					//set the JVM timezone to whatever the appliance is set to
					setJvmTimeZone();
					
					//Use EclipseLink MOXY as the jaxb provider.
					System.setProperty("javax.xml.bind.context.factory","org.eclipse.persistence.jaxb.JAXBContextFactory");
					
					// Note: We may need to move below DB init logic to
					// onStartup in initializer or to some
					// pre-processor. So that db init happens even before any
					// bean is initialized.
					// This could be needed as the beans may need to read from
					// DB during creation.
					// OR === ALTERNATE STRATEGY === 
					// bean constructor only inits the bean instance using props
					// file and the DB
					// reads happen when "start" is called on that bean
					// instance. This is the approach
					// followed in Pano code.
					dbService_.initializeDatabase();
					
					// Note: Initialize the logger before any logging calls are
					// made.
					// Database must be initialized before the logger can be
					// initiliazed.
					logger_.initialize();
					logger_.log(LogLevel.INFO, "Application.Starting", env_.getProperty("product.version"));
					startApplication();
					logger_.log(LogLevel.INFO, "Application.Started");

				} else if (event instanceof ContextClosedEvent) {
					logger_.log(LogLevel.INFO, "Application.Stopped");
					
					stopApplication();
				}
			}
		} catch (Exception e) {            
            throw new RuntimeException("Failed to start the application", e);
            // TODO: (Mandar). We should shutdown tomcat at this point.
		}
	}

	private void setJvmTimeZone() throws IOException, InterruptedException {
		if (RmsUtils.isLinux()) {
			List<String> output = new ArrayList<String>();
			ScriptUtil.executeScript("timedatectl | gawk -F'[: ]+' ' $2 ~ /Timezone/ {print $3}'", output);
			System.out.println("Got system timezone: " + output.get(0));
			System.setProperty("user.timezone", output.get(0));
			TimeZone.setDefault(null);
			Calendar cal = Calendar.getInstance();
			System.out.println("JVM timezone set to: " + cal.getTimeZone().getDisplayName());
		}
	}
	
	private boolean isRootContext(ApplicationContext context) {
		return context != null && context.getParent() == null;
	}

	private void startApplication() throws Exception {
		synchronized (lock_) {
			// Invoke start methods on individual services.
			searchService_.start();		
			directoryService_.start();
			tokenAuthenticationService_.start();
			authenticationService_.start();
			platformService_.start();
			
			rmsClusterService_.setApplicationService(this);
			rmsClusterService_.start();
			
			failoverService_.setApplicationService(this);
			failoverService_.start();

			// start event-subsystem
			eventBus_.register(uiEventService_);
			eventBus_.register(failoverService_);
			eventBus_.register(clientService_);
			eventBus_.register(groupService_);
			
			startSecondaryServices();
		}
	}

	// Note: ContextClosed event handler calls this method. This event is fired
	// even before @PreDestroy is called on any of the managed beans. We might be ok with
	// just using  @PreDestroy in future. Handling ContextClosed just gives us more control
	// over application shutdown sequence.
	private void stopApplication() throws Exception {
		synchronized (lock_) {
			stopSecondaryServices(false);

			// stop event-subsystem
			eventBus_.unregister(groupService_);
			eventBus_.unregister(clientService_);
			eventBus_.unregister(failoverService_);
			eventBus_.unregister(uiEventService_);
			
			// Invoke stop methods on individual services.
			failoverService_.stop();
			rmsClusterService_.stop();
			platformService_.stop();
			authenticationService_.stop();
			tokenAuthenticationService_.stop();
			directoryService_.stop();
			searchService_.stop();
			
			//Note: logger must be the last thing to stop.
			logger_.stop();
		}
	}
	
	/* (non-Javadoc)
	 * @see com.samsung.rms.core.services.api.app.ApplicationService#startSecondaryServices()
	 */
	@Override
	public void startSecondaryServices() throws Exception {
		synchronized (lock_) {
			metadataService_.start();
			schedulerService_.start();
			citrixConfigureService_.start();
			backupSettingsService_.start();
			backupService_.start();
			cacheService_.start();
			groupService_.start();
			clientService_.start();			
			profileService_.start();
			downloadHandlerService_.start();
			firmwareService_.start();
			emailConfigurationService_.start();
			taskService_.start();
			taskStatusService_.start();
			recentTaskService_.start();
			logCollectorService_.start();
			configurationStatusService_.start();
			systemInfoService_.start();
		}
	}

	/* (non-Javadoc)
	 * @see com.samsung.rms.core.services.api.app.ApplicationService#stopSecondaryServices(boolean)
	 */
	@Override
	public void stopSecondaryServices(boolean isEnteringStandby) throws Exception {
		synchronized (lock_) {
			configurationStatusService_.stop();
			logCollectorService_.stop();
			recentTaskService_.stop();
			taskStatusService_.stop();
			taskService_.stop();
			emailConfigurationService_.stop();
			firmwareService_.stop();
			downloadHandlerService_.stop();
			clientService_.stop();
			groupService_.stop();
			profileService_.stop();
			cacheService_.stop();
			backupService_.stop();
			backupSettingsService_.stop();
			citrixConfigureService_.stop();
			systemInfoService_.stop();
			// NOTE: Stopping scheduler during HA causes issues when you try to restart it after becoming ACTIVE.
			if (isEnteringStandby) {
				schedulerService_.standby();
			} else {
				schedulerService_.stop();
			}
			
			metadataService_.stop();
		}
	}

	// TODO (Mandar): Adding these because "call" method in ClusterService currently needs these for HA.
	// Find out a way to get rid of these and make ClusterService get the required refs in a different way.
	
	@Override
	public FailoverService getFailoverService() {
		return failoverService_;
	}
		
	@Override
	public boolean isFailoverConfigured() {
		if (failoverService_ != null) {
			return failoverService_.getFailoverConfiguration() != null;
		}
		return false;
	}

}
