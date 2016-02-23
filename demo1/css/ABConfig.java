

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping;

import com.google.common.eventbus.EventBus;
import com.googlecode.jsonrpc4j.spring.JsonServiceExporter;
import com.samsung.rms.adapter.api.RestAdapterVer1;
import com.samsung.rms.adapter.impl.RestAdapterVer1Impl;
import com.samsung.rms.common.api.logging.RmsLogger;
import com.samsung.rms.common.impl.logging.LogCatalog;
import com.samsung.rms.common.impl.logging.RmsLoggerImpl;
import com.samsung.rms.common.impl.logging.SearchLogHandler;
import com.samsung.rms.common.impl.logging.SyslogLogHandler;
import com.samsung.rms.common.util.Constants;
import com.samsung.rms.core.dao.api.backup.BackupSettingsDao;
import com.samsung.rms.core.dao.api.backup.RmsJdbcBackupDao;
import com.samsung.rms.core.dao.api.clients.ClientDao;
import com.samsung.rms.core.dao.api.db.RmsJdbcDao;
import com.samsung.rms.core.dao.api.directory.DirectoryDao;
import com.samsung.rms.core.dao.api.failover.ClusterConfigurationDao;
import com.samsung.rms.core.dao.api.failover.ClusterMemberDao;
import com.samsung.rms.core.dao.api.failover.FailoverConfigurationDao;
import com.samsung.rms.core.dao.api.failover.FailoverModeDao;
import com.samsung.rms.core.dao.api.groups.GroupDao;
import com.samsung.rms.core.dao.api.metadata.ServerMetadataDao;
import com.samsung.rms.core.dao.api.settings.CitrixConfigureDao;
import com.samsung.rms.core.dao.api.settings.EmailConfigurationDao;
import com.samsung.rms.core.dao.api.task.RecentTaskDao;
import com.samsung.rms.core.dao.api.task.TaskScheduleDao;
import com.samsung.rms.core.services.api.agent.AgentJsonRpcAdapter;
import com.samsung.rms.core.services.api.agent.AgentJsonRpcServer;
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
import com.samsung.rms.core.services.impl.agent.AgentJsonRpcAdapterVer1Impl;
import com.samsung.rms.core.services.impl.agent.AgentJsonRpcServerImpl;
import com.samsung.rms.core.services.impl.agent.RmsAgentFactory;
import com.samsung.rms.core.services.impl.app.ApplicationServiceImpl;
import com.samsung.rms.core.services.impl.backup.BackupServiceImpl;
import com.samsung.rms.core.services.impl.backup.BackupSettingsServiceImpl;
import com.samsung.rms.core.services.impl.cache.JvmHeapCacheServiceImpl;
import com.samsung.rms.core.services.impl.clients.ClientServiceImpl;
import com.samsung.rms.core.services.impl.clients.commands.ClientCommandsFactory;
import com.samsung.rms.core.services.impl.db.DbServiceImpl;
import com.samsung.rms.core.services.impl.download.DownloadHandlerServiceImpl;
import com.samsung.rms.core.services.impl.events.UIEventServiceImpl;
import com.samsung.rms.core.services.impl.failover.FailoverServiceImpl;
import com.samsung.rms.core.services.impl.failover.RmsClusterServiceImpl;
import com.samsung.rms.core.services.impl.firmware.FirmwareServiceImpl;
import com.samsung.rms.core.services.impl.groups.GroupServiceImpl;
import com.samsung.rms.core.services.impl.logcollector.LogCollectorServiceImpl;
import com.samsung.rms.core.services.impl.member.authentication.AuthenticationServiceImpl;
import com.samsung.rms.core.services.impl.member.directory.DirectoryServiceImpl;
import com.samsung.rms.core.services.impl.metadata.MetadataServiceImpl;
import com.samsung.rms.core.services.impl.platform.PlatformServiceImpl;
import com.samsung.rms.core.services.impl.profile.ProfileServiceImpl;
import com.samsung.rms.core.services.impl.scheduler.SchedulerServiceImpl;
import com.samsung.rms.core.services.impl.search.SearchServiceImpl;
import com.samsung.rms.core.services.impl.security.TokenAuthenticationServiceImpl;
import com.samsung.rms.core.services.impl.settings.CitrixConfigureServiceImpl;
import com.samsung.rms.core.services.impl.settings.ConfigurationStatusServiceImpl;
import com.samsung.rms.core.services.impl.settings.EmailConfigurationServiceImpl;
import com.samsung.rms.core.services.impl.systeminfo.SystemInfoServiceImpl;
import com.samsung.rms.core.services.impl.task.RecentTaskServiceImpl;
import com.samsung.rms.core.services.impl.task.TaskServiceImpl;
import com.samsung.rms.core.services.impl.task.TaskStatusServiceImpl;




/**
 * The Class CoreConfig.
 */
@Configuration
@Import({DaoConfig.class, QuartzConfig.class})
@PropertySources({
		@PropertySource("classpath:application.properties"),
		@PropertySource("classpath:revision.properties"),
		@PropertySource(value = "classpath:override.properties", ignoreResourceNotFound = true), })
public class CoreConfig {

	/**
	 * Property sources placeholder configurer.
	 *
	 * @return the property sources placeholder configurer
	 */
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	// TODO: We might want to copy the message resources to WEB-INF directory.
	// When loaded from classpath
	// the caching is in effect and loading new resource files as per locale
	// changes may not get reflected as
	// the cache is not flushed. So keep it in WEB-INF, change resourcepath
	// below to NOT use classpath and
	// then clear the cache when locale changes and load new resource bundle.
	//
	// REF:
	// http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/context/support/ReloadableResourceBundleMessageSource.html
	// [In contrast to ResourceBundleMessageSource, this class supports
	// reloading of properties files through
	// the "cacheSeconds" setting, and also through programmatically clearing
	// the properties cache. Since application
	// servers typically cache all files loaded from the classpath, it is
	// necessary to store resources somewhere else
	// (for example, in the "WEB-INF" directory of a web app). Otherwise changes
	// of files in the classpath will not be
	// reflected in the application.]

	/**
	 * Message source.
	 *
	 * @return the reloadable resource bundle message source
	 */
	@Bean
	public ReloadableResourceBundleMessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		String[] resources = { "classpath:/messages" };
		messageSource.setBasenames(resources);
		return messageSource;
	}

	// **** Adapter layer between Rest and Core layers.

	/**
	 * Rest adapter ver1.
	 *
	 * @param clientService the client service
	 * @param groupService the group service
	 * @param platformService the platform service
	 * @param backupSettingsService the backup settings service
	 * @param backupService the backup service
	 * @param directoryService the directory service
	 * @param authenticationService the authentication service
	 * @param searchService the search service
	 * @param citrixConfigService the citrix config service
	 * @param emailConfigurationService the email configuration service
	 * @param firmwareService the firmware service
	 * @param downloadHandlerService the download handler service
	 * @param taskService the task service
	 * @param recentTaskService the recent task service
	 * @param rmsClusterService the rms cluster service
	 * @param failoverService the failover service
	 * @param logCollectorService the log collector service
	 * @param configurationStatusService the configuration status service
	 * @param systemInfoService the system info service
	 * @param logger the logger
	 * @return the rest adapter ver1
	 */
	@Bean
	public RestAdapterVer1 restAdapterVer1(final ClientService clientService,
			final GroupService groupService,
			final PlatformService platformService,
			final BackupSettingsService backupSettingsService,
			final BackupService backupService,
			final DirectoryService directoryService,
			final AuthenticationService authenticationService,
			final SearchService searchService,
			final CitrixConfigureService citrixConfigService,
			final EmailConfigurationService emailConfigurationService,
			final FirmwareService firmwareService,
			final DownloadHandlerService downloadHandlerService,	
			final TaskService taskService,
			final RecentTaskService recentTaskService,			
			final RmsClusterService rmsClusterService,
			final FailoverService failoverService,			
			final LogCollectorService logCollectorService ,
			final ConfigurationStatusService configurationStatusService,
			final SystemInfoService systemInfoService,
			final RmsLogger logger) {
		return new RestAdapterVer1Impl(clientService, 
				groupService, 
				platformService,
				backupSettingsService,
				backupService,
				directoryService,
				authenticationService,
				searchService,
				citrixConfigService,
				emailConfigurationService,
				firmwareService,
				downloadHandlerService,
				taskService,
				recentTaskService,
				rmsClusterService,
				failoverService,				
				logCollectorService ,
				configurationStatusService,
				systemInfoService,
				logger);
	}
	
	/**
	 * Ui event service.
	 *
	 * @param logger the logger
	 * @param template the template
	 * @return the UI event service
	 */
	@Autowired
	@Bean
	public UIEventService uiEventService(RmsLogger logger,
			SimpMessagingTemplate template) {
		return new UIEventServiceImpl(logger, template);
	}
	
	// **** Json RPC server end point for agent communication
	/**
	 * Search server.
	 *
	 * @return the search server
	 */
	// **** Application level services bean definitions
	@Bean
	public SearchServer searchServer() {
		return new SearchServer();

	}
	
	/**
	 * Bean name url handler mapping.
	 *
	 * @return the bean name url handler mapping
	 */
	@Bean
	public BeanNameUrlHandlerMapping beanNameUrlHandlerMapping() {
		return new org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping();
	}

	/**
	 * Agent json rpc adapter.
	 *
	 * @param clientService the client service
	 * @param profileService the profile service
	 * @param recentTaskService the recent task service
	 * @param rmsLogger the rms logger
	 * @return the agent json rpc adapter
	 * @throws Exception the exception
	 */
	@Bean
	public AgentJsonRpcAdapter agentJsonRpcAdapter(ClientService clientService, ProfileService profileService, RecentTaskService recentTaskService, RmsLogger rmsLogger) throws Exception {
		return new AgentJsonRpcAdapterVer1Impl(clientService, profileService, recentTaskService, rmsLogger);
	}	
	
	/**
	 * Agent json rpc server.
	 *
	 * @param agentJsonRpcAdapter the agent json rpc adapter
	 * @param rmsLogger the rms logger
	 * @return the agent json rpc server
	 */
	@Bean
	public AgentJsonRpcServer agentJsonRpcServer(AgentJsonRpcAdapter agentJsonRpcAdapter, RmsLogger rmsLogger) {
		return new AgentJsonRpcServerImpl(agentJsonRpcAdapter, rmsLogger);
	}

	/**
	 * Json service exporter.
	 *
	 * @param agentJsonRpcServer the agent json rpc server
	 * @return the json service exporter
	 */
	@Bean(name = Constants.AGENT_API_URL)
	public JsonServiceExporter jsonServiceExporter(
			AgentJsonRpcServer agentJsonRpcServer) {
		JsonServiceExporter jsonRpcServiceExporter = new JsonServiceExporter();
		jsonRpcServiceExporter.setService(agentJsonRpcServer);
		jsonRpcServiceExporter.setServiceInterface(AgentJsonRpcServer.class);
		return jsonRpcServiceExporter;
	}

	/**
	 * Log catalog.
	 *
	 * @return the log catalog
	 */
	@Bean
	public LogCatalog logCatalog() {
		return new LogCatalog();
	}

	/**
	 * Search log handler.
	 *
	 * @param searchServer the search server
	 * @param logCatalog the log catalog
	 * @return the search log handler
	 */
	@Bean
	public SearchLogHandler searchLogHandler(SearchServer searchServer,
			LogCatalog logCatalog) {
		return new SearchLogHandler(searchServer, logCatalog);
	}

	/**
	 * Syslog handler.
	 *
	 * @param logCatalog the log catalog
	 * @return the syslog log handler
	 */
	@Bean
	public SyslogLogHandler syslogHandler(LogCatalog logCatalog) {
		return new SyslogLogHandler(logCatalog);
	}

	/**
	 * Rms logger.
	 *
	 * @param logCatalog the log catalog
	 * @param searchLogHandler the search log handler
	 * @param syslogLogHandler the syslog log handler
	 * @return the rms logger
	 */
	@Bean
	public RmsLogger rmsLogger(LogCatalog logCatalog,
			SearchLogHandler searchLogHandler, SyslogLogHandler syslogLogHandler) {
		return new RmsLoggerImpl(logCatalog, searchLogHandler, syslogLogHandler);
	}

	/**
	 * Db service.
	 *
	 * @param rmsJdbcDao the rms jdbc dao
	 * @return the db service
	 */
	@Bean
	public DbService dbService(RmsJdbcDao rmsJdbcDao) {
		return new DbServiceImpl(rmsJdbcDao);
	}

	/**
	 * Rms agent factory.
	 *
	 * @param logger the logger
	 * @return the rms agent factory
	 */
	@Bean
	public RmsAgentFactory rmsAgentFactory(RmsLogger logger) {
		return new RmsAgentFactory(logger);
	}

	/**
	 * Metadata service.
	 *
	 * @param metadataDao the metadata dao
	 * @param logger the logger
	 * @return the metadata service
	 */
	@Bean
	public MetadataService metadataService(ServerMetadataDao metadataDao,
			RmsLogger logger) {
		return new MetadataServiceImpl(metadataDao, logger);
	}

	/**
	 * Client service.
	 *
	 * @param clientDao the client dao
	 * @param metadataService the metadata service
	 * @param cacheService the cache service
	 * @param rmsAgentFactory the rms agent factory
	 * @param groupService the group service
	 * @param recentTaskService the recent task service
	 * @param logger the logger
	 * @param eventBus the event bus
	 * @param clientCommandsFactory the client commands factory
	 * @return the client service
	 */
	@Bean
	public ClientService clientService(
			ClientDao clientDao,
			MetadataService metadataService,
			CacheService cacheService, RmsAgentFactory rmsAgentFactory,
			GroupService groupService, RecentTaskService recentTaskService, RmsLogger logger, EventBus eventBus, ClientCommandsFactory clientCommandsFactory) {
		return new ClientServiceImpl(clientDao, metadataService,
				cacheService, rmsAgentFactory, groupService, recentTaskService, logger, eventBus, clientCommandsFactory);
	}
	

	/**
	 * Group service.
	 *
	 * @param groupDao the group dao
	 * @param cacheService the cache service
	 * @param logger the logger
	 * @param eventBus the event bus
	 * @return the group service
	 */
	@Bean
	public GroupService groupService(GroupDao groupDao, CacheService cacheService, RmsLogger logger, EventBus eventBus) {
		return new GroupServiceImpl(groupDao, cacheService, eventBus, logger);
	}
	
	/**
	 * Profile service.
	 *
	 * @param citrixConfigureService the citrix configure service
	 * @param logger the logger
	 * @return the profile service
	 */
	@Bean
	public ProfileService profileService(CitrixConfigureService citrixConfigureService, RmsLogger logger) {
		return new ProfileServiceImpl(citrixConfigureService, logger);
	}

	/**
	 * Platform service.
	 *
	 * @param eventbus the eventbus
	 * @param logger the logger
	 * @return the platform service
	 */
	@Bean
	public PlatformService platformService(EventBus eventbus, RmsLogger logger) {
		return new PlatformServiceImpl(eventbus, logger);
	}
	
	/**
	 * Cache service.
	 *
	 * @param logger the logger
	 * @return the cache service
	 */
	@Bean
	public CacheService cacheService(RmsLogger logger) {
		return new JvmHeapCacheServiceImpl(logger);
	}

	/**
	 * Search service.
	 *
	 * @param searchServer the search server
	 * @param downloadHandlerService the download handler service
	 * @param schedulerService the scheduler service
	 * @param logger the logger
	 * @return the search service
	 */
	@Bean
	public SearchService searchService(SearchServer searchServer,
			DownloadHandlerService downloadHandlerService,
			SchedulerService schedulerService,
			RmsLogger logger) {
		return new SearchServiceImpl(searchServer, downloadHandlerService, schedulerService, logger);
	}
	
	/**
	 * Application service.
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
	 * @return the application service
	 */
	@Bean
	public ApplicationService applicationService(final DbService dbService,
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
			final RmsClusterService rmsClusterService, 
			final FailoverService failoverService,
			final EventBus eventBus,
			final UIEventService uiEventService,
			final TaskService taskService,
			final TaskStatusService taskStatusService,
			final RecentTaskService recentTaskService,
			final LogCollectorService logCollectorService,
			final ConfigurationStatusService configurationStatusService,
			final SystemInfoService systemInfoService,
			final RmsLogger rmsLogger) {
		return new ApplicationServiceImpl(dbService,
				clientService,
				groupService,
				profileService,
				platformService,
				metadataService,
				cacheService,
				searchService,
				backupSettingsService,
				backupService,
				schedulerService,
				directoryService,
				authenticationService,
				tokenAuthenticationService,
				citrixConfigureService,
				emailConfigurationService,
				firmwareService,
				downloadChunkHandlerService,
				rmsClusterService,
				failoverService,
				eventBus,
				uiEventService,
				taskService,
				taskStatusService,
				recentTaskService,
				logCollectorService,
				configurationStatusService,
				systemInfoService,
				rmsLogger);
	}

	/**
	 * Backup settings service.
	 *
	 * @param settingsDao the settings dao
	 * @param eventBus the event bus
	 * @param logger the logger
	 * @return the backup settings service
	 */
	@Bean
	public BackupSettingsService backupSettingsService(
			BackupSettingsDao settingsDao, EventBus eventBus, RmsLogger logger) {
		return new BackupSettingsServiceImpl(settingsDao, eventBus, logger);
	}

	/**
	 * Backup service.
	 *
	 * @param backupDao the backup dao
	 * @param backupSettingService the backup setting service
	 * @param schedulerService the scheduler service
	 * @param platformService the platform service
	 * @param eventBus the event bus
	 * @param logger the logger
	 * @return the backup service
	 */
	@Bean
	public BackupService backupService(RmsJdbcBackupDao backupDao,
			BackupSettingsService backupSettingService,
			SchedulerService schedulerService, PlatformService platformService, EventBus eventBus, RmsLogger logger) {
		return new BackupServiceImpl(backupDao, backupSettingService,
				schedulerService, platformService, eventBus, logger);
	}
	
	/**
	 * Citrix configure service.
	 *
	 * @param settingsDao the settings dao
	 * @param eventBus the event bus
	 * @param logger the logger
	 * @return the citrix configure service
	 */
	@Bean
	public CitrixConfigureService citrixConfigureService(
			CitrixConfigureDao settingsDao,  EventBus eventBus,RmsLogger logger) {
		return new CitrixConfigureServiceImpl(settingsDao, eventBus, logger);
	}
	
	
	/**
	 * Configuration status service.
	 *
	 * @param citrixConfigureService the citrix configure service
	 * @param platformService the platform service
	 * @param emailConfigurationService the email configuration service
	 * @param backupSettingsService the backup settings service
	 * @param directoryService the directory service
	 * @param failoverService the failover service
	 * @param logger the logger
	 * @return the configuration status service
	 */
	@Bean
	public ConfigurationStatusService configurationStatusService(CitrixConfigureService citrixConfigureService,
			PlatformService platformService,
			EmailConfigurationService emailConfigurationService,
			BackupSettingsService  backupSettingsService,
			DirectoryService directoryService,
			FailoverService failoverService,
			RmsLogger logger){
		   return new ConfigurationStatusServiceImpl(citrixConfigureService, platformService, emailConfigurationService, backupSettingsService, directoryService, failoverService, logger);
	}


	/**
	 * Directory service.
	 *
	 * @param ldapDao the ldap dao
	 * @param eventBus the event bus
	 * @param logger the logger
	 * @return the directory service
	 */
	@Bean
	public DirectoryService directoryService(DirectoryDao ldapDao, EventBus eventBus,
			RmsLogger logger) {
		return new DirectoryServiceImpl(ldapDao, eventBus , logger);
	}
	
	/**
	 * Authentication service.
	 *
	 * @param logger the logger
	 * @param ldapDao the ldap dao
	 * @param directoryService the directory service
	 * @param eventBus the event bus
	 * @return the authentication service
	 * @throws Throwable the throwable
	 */
	@Bean
	public AuthenticationService authenticationService(final RmsLogger logger, 
			DirectoryDao ldapDao, DirectoryService directoryService,
			EventBus eventBus) throws Throwable
			{
		return new AuthenticationServiceImpl( logger,ldapDao, directoryService, eventBus);
	}

	/**
	 * Token authentication service.
	 *
	 * @param logger the logger
	 * @return the token authentication service
	 */
	@Bean
	public TokenAuthenticationService tokenAuthenticationService(
			final RmsLogger logger){
		return new TokenAuthenticationServiceImpl( logger);
	}

	/**
	 * Scheduler service.
	 *
	 * @param schedulerFactoryBean the scheduler factory bean
	 * @param logger the logger
	 * @return the scheduler service
	 */
	@Bean
	public SchedulerService schedulerService(
			final SchedulerFactoryBean schedulerFactoryBean,
			final RmsLogger logger) {
		return new SchedulerServiceImpl(schedulerFactoryBean.getScheduler(), logger);
	}

	/**
	 * Multipart resolver.
	 *
	 * @return the multipart resolver
	 */
	@Bean(name = "multipartResolver")
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

	/**
	 * Firmware service.
	 *
	 * @param clientService the client service
	 * @param cacheService the cache service
	 * @param eventBus the event bus
	 * @param taskStatusService the task status service
	 * @param recentTaskService the recent task service
	 * @param logger the logger
	 * @return the firmware service
	 */
	@Bean
	public FirmwareService firmwareService(final ClientService clientService,
			final CacheService cacheService, final EventBus eventBus,
			final TaskStatusService taskStatusService,
			final RecentTaskService recentTaskService,
			final RmsLogger logger) {
		return new FirmwareServiceImpl(clientService, cacheService , eventBus, taskStatusService, recentTaskService, logger);
	}

	/**
	 * Download handler service.
	 *
	 * @param eventBus the event bus
	 * @param logger the logger
	 * @return the download handler service
	 */
	@Bean
	public DownloadHandlerService downloadHandlerService(
			final EventBus eventBus, final RmsLogger logger) {
		return new DownloadHandlerServiceImpl(eventBus, logger);
	}
	
	/**
	 * Rms cluster service.
	 *
	 * @param clusterConfigurationDao the cluster configuration dao
	 * @param clusterMemberDao the cluster member dao
	 * @param rmsLogger the rms logger
	 * @param eventBus the event bus
	 * @return the rms cluster service
	 */
	@Bean
	public RmsClusterService rmsClusterService(
			final ClusterConfigurationDao clusterConfigurationDao,
			final ClusterMemberDao clusterMemberDao, final RmsLogger rmsLogger,
			final EventBus eventBus) {
		return new RmsClusterServiceImpl(clusterConfigurationDao,
				clusterMemberDao, rmsLogger, eventBus);
	}
	
	/**
	 * Email configuration service.
	 *
	 * @param emailConfigurationDao the email configuration dao
	 * @param eventBus the event bus
	 * @param logger the logger
	 * @return the email configuration service
	 */
	@Bean
	public EmailConfigurationService emailConfigurationService(
			EmailConfigurationDao emailConfigurationDao, EventBus eventBus, RmsLogger logger) {
		return new EmailConfigurationServiceImpl(emailConfigurationDao,eventBus,logger);
	}

	/**
	 * Failover service.
	 *
	 * @param rmsClusterService the rms cluster service
	 * @param failoverConfigurationDao the failover configuration dao
	 * @param failoverModeDao the failover mode dao
	 * @param authenticationService the authentication service
	 * @param directoryService the directory service
	 * @param rmsJdbcBackupDao the rms jdbc backup dao
	 * @param emailConfigurationService the email configuration service
	 * @param platformService the platform service
	 * @param eventBus the event bus
	 * @param rmsLogger the rms logger
	 * @return the failover service
	 */
	@Bean
	public FailoverService failoverService(
			final RmsClusterService rmsClusterService,
			final FailoverConfigurationDao failoverConfigurationDao,
			final FailoverModeDao failoverModeDao,
			final AuthenticationService authenticationService,
			final DirectoryService directoryService,
			final RmsJdbcBackupDao rmsJdbcBackupDao,
			final EmailConfigurationService emailConfigurationService,
			final PlatformService platformService,
			final EventBus eventBus,
			final RmsLogger rmsLogger) {
		return new FailoverServiceImpl(rmsClusterService, failoverConfigurationDao, failoverModeDao, authenticationService, directoryService, emailConfigurationService, platformService, rmsJdbcBackupDao, eventBus, rmsLogger);
	}
	
	/**
	 * Task service.
	 *
	 * @param clientService the client service
	 * @param firmwareService the firmware service
	 * @param taskStatusService the task status service
	 * @param taskScheduleDao the task schedule dao
	 * @param schedulerService the scheduler service
	 * @param eventBus the event bus
	 * @param logger the logger
	 * @return the task service
	 */
	@Bean
	public TaskService taskService(
			final ClientService clientService,
			final FirmwareService firmwareService,
			final TaskStatusService taskStatusService,
			final TaskScheduleDao taskScheduleDao,
			final SchedulerService schedulerService, EventBus eventBus,
			final RmsLogger logger) {
		
		return new TaskServiceImpl(clientService, firmwareService, taskStatusService, taskScheduleDao, schedulerService, eventBus, logger);
	}
	
	/**
	 * Task status service.
	 *
	 * @param taskScheduleDao the task schedule dao
	 * @param eventBus the event bus
	 * @param logger the logger
	 * @return the task status service
	 */
	@Bean
	public TaskStatusService taskStatusService(final TaskScheduleDao taskScheduleDao, final EventBus eventBus, final RmsLogger logger) {
	  return new TaskStatusServiceImpl(taskScheduleDao, eventBus, logger);
	}
	
	/**
	 * Log collector service.
	 *
	 * @param clientService the client service
	 * @param eventBus the event bus
	 * @param logger the logger
	 * @return the log collector service
	 */
	@Bean
	public LogCollectorService logCollectorService(final ClientService clientService, final EventBus eventBus, final RmsLogger logger) {
		return new LogCollectorServiceImpl(clientService,eventBus, logger);
	}
	
	/**
	 * Recent task service.
	 *
	 * @param schedulerService the scheduler service
	 * @param taskStatusService the task status service
	 * @param recentTaskDao the recent task dao
	 * @param eventBus the event bus
	 * @param logger the logger
	 * @return the recent task service
	 */
	@Bean
	public RecentTaskService recentTaskService(
			final SchedulerService schedulerService,
			final TaskStatusService taskStatusService,
			final RecentTaskDao recentTaskDao,
			final EventBus eventBus,
			final RmsLogger logger) {
		
		return new RecentTaskServiceImpl(schedulerService, taskStatusService, recentTaskDao, eventBus, logger);
	}
	
	/**
	 * System info service.
	 *
	 * @param logger the logger
	 * @return the system info service
	 */
	@Bean
	public SystemInfoService systemInfoService(final RmsLogger logger){
	  return new SystemInfoServiceImpl(logger);
	}
	
	/**
	 * Client commands factory.
	 *
	 * @param cacheService the cache service
	 * @param metadataService the metadata service
	 * @param logger the logger
	 * @return the client commands factory
	 */
	@Bean
	public ClientCommandsFactory clientCommandsFactory(final CacheService cacheService, final MetadataService metadataService, final RmsLogger logger){
		return new ClientCommandsFactory(cacheService, metadataService, logger);
	}
	
}
