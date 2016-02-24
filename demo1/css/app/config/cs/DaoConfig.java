package com.samsung.rms.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.hibernate.SessionFactory;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.EnvironmentStringPBEConfig;
import org.jasypt.hibernate4.encryptor.HibernatePBEStringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.google.common.eventbus.EventBus;
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
import com.samsung.rms.core.dao.impl.backup.BackupSettingsDaoImpl;
import com.samsung.rms.core.dao.impl.backup.RmsJdbcBackupDaoImpl;
import com.samsung.rms.core.dao.impl.clients.ClientDaoImpl;
import com.samsung.rms.core.dao.impl.db.RmsJdbcDaoImpl;
import com.samsung.rms.core.dao.impl.directory.DirectoryDaoImpl;
import com.samsung.rms.core.dao.impl.failover.ClusterConfigurationDaoImpl;
import com.samsung.rms.core.dao.impl.failover.ClusterMemberDaoImpl;
import com.samsung.rms.core.dao.impl.failover.FailoverConfigurationDaoImpl;
import com.samsung.rms.core.dao.impl.failover.FailoverModeDaoImpl;
import com.samsung.rms.core.dao.impl.groups.GroupDaoImpl;
import com.samsung.rms.core.dao.impl.metadata.ServerMetadataDaoImpl;
import com.samsung.rms.core.dao.impl.settings.CitrixConfigureDaoImpl;
import com.samsung.rms.core.dao.impl.settings.EmailConfigurationDaoImpl;
import com.samsung.rms.core.dao.impl.task.RecentTaskDaoImpl;
import com.samsung.rms.core.dao.impl.task.TaskScheduleDaoImpl;
import com.samsung.rms.core.domain.backup.BackupSettings;
import com.samsung.rms.core.domain.clients.Client;
import com.samsung.rms.core.domain.directory.AccountInfo;
import com.samsung.rms.core.domain.directory.DirectorySettings;
import com.samsung.rms.core.domain.directory.UserRoleConfiguration;
import com.samsung.rms.core.domain.failover.ClusterConfiguration;
import com.samsung.rms.core.domain.failover.FailoverConfiguration;
import com.samsung.rms.core.domain.failover.FailoverModeDbWrapper;
import com.samsung.rms.core.domain.failover.SimpleClusterMember;
import com.samsung.rms.core.domain.groups.ClientPreferences;
import com.samsung.rms.core.domain.groups.Group;
import com.samsung.rms.core.domain.groups.GroupProfile;
import com.samsung.rms.core.domain.groups.KeyboardLayout;
import com.samsung.rms.core.domain.groups.Language;
import com.samsung.rms.core.domain.groups.MonitorResolution;
import com.samsung.rms.core.domain.groups.MonitorSaveTime;
import com.samsung.rms.core.domain.groups.Timezone;
import com.samsung.rms.core.domain.metadata.ServerMetadata;
import com.samsung.rms.core.domain.settings.CitrixConfiguration;
import com.samsung.rms.core.domain.settings.EmailConfiguration;
import com.samsung.rms.core.domain.task.ClientTaskSchedule;
import com.samsung.rms.core.domain.task.Task;
import com.samsung.rms.core.domain.task.TaskSchedule;
import com.samsung.rms.core.domain.task.TaskType;
import com.samsung.rms.core.domain.task.TrackableTask;




/**
 * The Class DaoConfig.
 */
@Configuration
@EnableTransactionManagement
@PropertySource("classpath:application.properties")
public class DaoConfig {

	@Value("${jdbc.driverClassName}")
	private String jdbcDriverClassName;

	@Value("${jdbc.databaseurl}")
	private String jdbcDatabaseUrl;

	@Value("${jdbc.username}")
	private String jdbcUsername;

	@Value("${jdbc.password}")
	private String jdbcPassword;

	@Value("${hibernate.dialect}")
	private String hibernateDialect;

	@Value("${hibernate.show_sql}")
	private String hibernateShowSql;

	@Value("${hibernate.format_sql}")
	private String hibernateFormatSql;
	
	@Value("${hibernate.encrypt_password}")
    private String ENC_PASSWD = "PBEWithMD5AndDESPBEWithMD5AndDESPBEWithMD5AndDESPBEWithMD5AndDES";

	@Value("${dbcp.initialSize}")
	private int dbcpInitialSize;
	
	@Value("${dbcp.maxTotal}")
	private int dbcpMaxTotal;
	
	@Value("${dbcp.maxIdle}")
	private int dbcpMaxIdle;
	
	@Value("${dbcp.minIdle}")
	private int dbcpMinIdle;
	
	/**
	 * Place holder configurer.
	 *
	 * @return the property sources placeholder configurer
	 */
	@Bean
	public static PropertySourcesPlaceholderConfigurer placeHolderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	// So as to translate all Persistence api specific exceptions to Unchecked
	/**
	 * Persistence exception translation post processor.
	 *
	 * @return the persistence exception translation post processor
	 */
	// Spring Exceptions.
	@Bean
	public PersistenceExceptionTranslationPostProcessor persistenceExceptionTranslationPostProcessor() {
		return new PersistenceExceptionTranslationPostProcessor();
	}

	//TODO (Mandar): Get rid of this from DaoConfig. Entire DBupdateEvent Firing from Dao classes
	// should be driven by AOP. Mark Dao method with @SyncDB and After-advice. Then in advice
	// check for presence of @SyncDB and if present fire the event.
	
	// **** Event subsystem based on guava eventbus. Same evenbus instance is shared across all
	/**
	 * Event bus.
	 *
	 * @return the event bus
	 */
	// server components for publish, subscribe.
	@Bean
	public EventBus eventBus() {
		return new EventBus("eventbus.rms");
	}

	@Bean(name = "dataSource")
	public DataSource getDataSource() {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(jdbcDriverClassName);
		dataSource.setUrl(jdbcDatabaseUrl);
		dataSource.setUsername(jdbcUsername);
		dataSource.setPassword(jdbcPassword);
		dataSource.setInitialSize(dbcpInitialSize);
		dataSource.setMaxTotal(dbcpMaxTotal);
		dataSource.setMaxIdle(dbcpMaxIdle);
		dataSource.setMinIdle(dbcpMinIdle);	
		


		return dataSource;
	}

	/**
	 * Gets the session factory.
	 *
	 * @param dataSource the data source
	 * @return the session factory
	 */
	@Autowired
	@Bean(name = "sessionFactory")
	public SessionFactory getSessionFactory(DataSource dataSource) {
    LocalSessionFactoryBuilder sessionBuilder = new LocalSessionFactoryBuilder(dataSource);
		sessionBuilder.addAnnotatedClasses(Client.class);
		sessionBuilder.addAnnotatedClasses(ServerMetadata.class);
		sessionBuilder.addAnnotatedClasses(BackupSettings.class);
	    sessionBuilder.addAnnotatedClasses(AccountInfo.class);
	    sessionBuilder.addAnnotatedClasses(UserRoleConfiguration.class);
	    sessionBuilder.addAnnotatedClasses(DirectorySettings.class);
		sessionBuilder.addAnnotatedClasses(CitrixConfiguration.class);
		sessionBuilder.addAnnotatedClass(ClusterConfiguration.class);
		sessionBuilder.addAnnotatedClass(FailoverConfiguration.class);
		sessionBuilder.addAnnotatedClass(SimpleClusterMember.class);
		sessionBuilder.addAnnotatedClasses(EmailConfiguration.class);
		
		sessionBuilder.addAnnotatedClasses(TaskScheduleDao.class);
	    sessionBuilder.addAnnotatedClasses(TaskType.class);
	    sessionBuilder.addAnnotatedClasses(ClientTaskSchedule.class);
	    sessionBuilder.addAnnotatedClasses(TaskSchedule.class);
	    sessionBuilder.addAnnotatedClasses(Task.class);
		sessionBuilder.addAnnotatedClass(FailoverModeDbWrapper.class);
		sessionBuilder.addAnnotatedClasses(Group.class);
		sessionBuilder.addAnnotatedClasses(GroupProfile.class);
		sessionBuilder.addAnnotatedClasses(ClientPreferences.class);
		sessionBuilder.addAnnotatedClasses(Timezone.class);
		sessionBuilder.addAnnotatedClasses(KeyboardLayout.class);
		sessionBuilder.addAnnotatedClasses(Language.class);
		sessionBuilder.addAnnotatedClasses(MonitorResolution.class);
		sessionBuilder.addAnnotatedClasses(MonitorSaveTime.class);
		
		sessionBuilder.addAnnotatedClasses(TrackableTask.class);
		sessionBuilder.addProperties(getHibernateProperties());
	    return sessionBuilder.buildSessionFactory();
	}

	private Properties getHibernateProperties() {
		Properties properties = new Properties();
		properties.put("hibernate.dialect", hibernateDialect);
		properties.put("hibernate.format_sql", hibernateFormatSql);
		properties.put("hibernate.show_sql", hibernateShowSql);

		// TODO: set default hibernate properties

		return properties;
	}

	/**
	 * Gets the transaction manager.
	 *
	 * @param sessionFactory the session factory
	 * @return the transaction manager
	 */
	@Bean(name = "transactionManager")
	public HibernateTransactionManager getTransactionManager(SessionFactory sessionFactory) {
		return new HibernateTransactionManager(sessionFactory);

	}

	/**
	 * Gets the client dao.
	 *
	 * @param sessionFactory the session factory
	 * @param eventBus the event bus
	 * @return the client dao
	 */
	@Bean(name = "clientDao")
	public ClientDao getClientDao(SessionFactory sessionFactory, EventBus eventBus) {
		return new ClientDaoImpl(sessionFactory, eventBus);
	}

	/**
	 * Gets the profile dao.
	 *
	 * @param sessionFactory the session factory
	 * @param eventBus the event bus
	 * @return the profile dao
	 */
	@Bean(name = "profileDao")
	public GroupDao getprofileDao(SessionFactory sessionFactory, EventBus eventBus) {
		return new GroupDaoImpl(sessionFactory, eventBus);
	}
	
	/**
	 * Metadata dao.
	 *
	 * @param sessionFactory the session factory
	 * @param eventBus the event bus
	 * @return the server metadata dao
	 */
	@Bean
	public ServerMetadataDao metadataDao(SessionFactory sessionFactory, EventBus eventBus) {
		return new ServerMetadataDaoImpl(sessionFactory, eventBus);
	}

	/**
	 * Rms jdbc dao.
	 *
	 * @param dataSource the data source
	 * @param eventBus the event bus
	 * @return the rms jdbc dao
	 */
	@Bean
	public RmsJdbcDao rmsJdbcDao(DataSource dataSource, EventBus eventBus) {
		return new RmsJdbcDaoImpl(dataSource, eventBus);
	}

	/**
	 * Backup settings dao.
	 *
	 * @param sessionFactory the session factory
	 * @param eventBus the event bus
	 * @return the backup settings dao
	 */
	@Bean
	public BackupSettingsDao backupSettingsDao(SessionFactory sessionFactory, EventBus eventBus) {
		return new BackupSettingsDaoImpl(sessionFactory, eventBus);
	}
	
	/**
	 * Citrix config dao.
	 *
	 * @param sessionFactory the session factory
	 * @param eventBus the event bus
	 * @return the citrix configure dao
	 */
	@Bean
	public CitrixConfigureDao citrixConfigDao(SessionFactory sessionFactory, EventBus eventBus) {
		return new CitrixConfigureDaoImpl(sessionFactory, eventBus);
	}
	
	/**
	 * Backup dao.
	 *
	 * @param dataSource the data source
	 * @param eventBus the event bus
	 * @param metadataDao the metadata dao
	 * @param clusterConfigurationDao the cluster configuration dao
	 * @param clusterMemberDao the cluster member dao
	 * @param failoverConfigurationDao the failover configuration dao
	 * @param failoverModeDao the failover mode dao
	 * @return the rms jdbc backup dao
	 */
	@Bean
	public RmsJdbcBackupDao backupDao(DataSource dataSource, EventBus eventBus, ServerMetadataDao metadataDao, 
			ClusterConfigurationDao clusterConfigurationDao, ClusterMemberDao clusterMemberDao,
			FailoverConfigurationDao failoverConfigurationDao, FailoverModeDao failoverModeDao) {
		return new RmsJdbcBackupDaoImpl(dataSource, eventBus, metadataDao, clusterConfigurationDao, clusterMemberDao,
				failoverConfigurationDao, failoverModeDao);
	}

	/**
	 * Email configuration dao.
	 *
	 * @param sessionFactory the session factory
	 * @param eventBus the event bus
	 * @return the email configuration dao
	 */
	@Autowired
	@Bean
	public EmailConfigurationDao emailConfigurationDao(SessionFactory sessionFactory, EventBus eventBus) {
		return new EmailConfigurationDaoImpl(sessionFactory, eventBus);
	}

	/**
	 * Ldap dao.
	 *
	 * @param sessionFactory the session factory
	 * @param eventBus the event bus
	 * @return the directory dao
	 */
	@Bean
	public DirectoryDao ldapDAO(SessionFactory sessionFactory, EventBus eventBus) {
		return new DirectoryDaoImpl(sessionFactory, eventBus);
	}
	
	/**
	 * Cluster configuration dao.
	 *
	 * @param sessionFactory the session factory
	 * @param eventBus the event bus
	 * @return the cluster configuration dao
	 */
	@Bean
	public ClusterConfigurationDao clusterConfigurationDao(SessionFactory sessionFactory, EventBus eventBus) {
		return new ClusterConfigurationDaoImpl(sessionFactory, eventBus);
	}
	
	/**
	 * Cluster member dao.
	 *
	 * @param sessionFactory the session factory
	 * @param eventBus the event bus
	 * @return the cluster member dao
	 */
	@Bean
	public ClusterMemberDao clusterMemberDao(SessionFactory sessionFactory, EventBus eventBus) {
		return new ClusterMemberDaoImpl(sessionFactory, eventBus);
	}
	
	/**
	 * Failover configuration dao.
	 *
	 * @param sessionFactory the session factory
	 * @return the failover configuration dao
	 */
	@Bean
	public FailoverConfigurationDao failoverConfigurationDao(SessionFactory sessionFactory) {
		return new FailoverConfigurationDaoImpl(sessionFactory);
	}
	
	/**
	 * Failover mode dao.
	 *
	 * @param sessionFactory the session factory
	 * @return the failover mode dao
	 */
	@Bean
	public FailoverModeDao failoverModeDao(SessionFactory sessionFactory) {
		return new FailoverModeDaoImpl(sessionFactory);
	}

	/**
	 * Task schedule dao.
	 *
	 * @param sessionFactory the session factory
	 * @param eventBus the event bus
	 * @return the task schedule dao
	 */
	@Autowired
	@Bean
	public TaskScheduleDao taskScheduleDao(SessionFactory sessionFactory, EventBus eventBus) {
		return new TaskScheduleDaoImpl(sessionFactory, eventBus);
	}
	
	/**
	 * Recent task dao.
	 *
	 * @param sessionFactory the session factory
	 * @param eventBus the event bus
	 * @return the recent task dao
	 */
	@Autowired
	@Bean
	public RecentTaskDao recentTaskDao(SessionFactory sessionFactory, EventBus eventBus) {
		return new RecentTaskDaoImpl(sessionFactory, eventBus);
	}
	
	
	/**
	 * Hibernate encryption.
	 *
	 * @return the environment string pbe config
	 */
	@Bean
	public EnvironmentStringPBEConfig encryptorConfiguration(){
		EnvironmentStringPBEConfig obj = new EnvironmentStringPBEConfig();
		obj.setAlgorithm("PBEWithMD5AndDES");
		obj.setPassword(ENC_PASSWD);
		return obj;
	}
	
	/**
	 * Encryptor.
	 *
	 * @return the standard pbe string encryptor
	 */
	@Bean
	public StandardPBEStringEncryptor encryptor(){
		StandardPBEStringEncryptor obj = new StandardPBEStringEncryptor();
		obj.setConfig(encryptorConfiguration());
		return obj;
	}
	
	/**
	 * Hibernate string encryptor.
	 *
	 * @return the hibernate pbe string encryptor
	 */
	@Bean
	public HibernatePBEStringEncryptor hibernateStringEncryptor(){
		HibernatePBEStringEncryptor obj = new HibernatePBEStringEncryptor();
		obj.setEncryptor(encryptor());
		obj.setRegisteredName("hibernateStringEncryptor");
		return obj;
	}

}
