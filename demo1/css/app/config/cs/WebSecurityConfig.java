package com.samsung.rms.config;

import static com.samsung.rms.common.util.Constants.RMS_API_URL_V1;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.samsung.rms.common.util.Constants;
import com.samsung.rms.core.filters.StatelessAuthenticationFilter;
import com.samsung.rms.core.filters.StatelessLdapLoginFilter;
import com.samsung.rms.core.services.api.member.authentication.AuthenticationService;
import com.samsung.rms.core.services.api.security.TokenAuthenticationService;
import com.samsung.rms.core.services.impl.security.CustomAuthenticationProvider;
import com.samsung.rms.core.services.impl.security.HttpLogoutSuccessHandler;




/**
 * The Class WebSecurityConfig.
 */
@Configuration
@EnableWebSecurity
@PropertySource("classpath:application.properties")
@ComponentScan(basePackages = {"com.samsung.rms.core.services.impl.security"})
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {


    @Value("${ldap.loginUrl}")
    private String LOGIN_PATH = RMS_API_URL_V1+"/auth/login";
    
    @Value("${ldap.logoutUrl}")
    private String LOGOUT_PATH = RMS_API_URL_V1+"/auth/logout";
    
  
    @Autowired
    private HttpLogoutSuccessHandler logoutSuccessHandler;
    
    @Autowired
    private AuthenticationService authenticationService;
    
    @Autowired
    private TokenAuthenticationService tokenAuthenticationService;

    /**
     * Instantiates a new web security config.
     */
    public WebSecurityConfig(){
    	super(true);
    }
    
    /* (non-Javadoc)
     * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#authenticationManagerBean()
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * Authentication provider.
     *
     * @return the authentication provider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {  
    	AuthenticationProvider provider= null;
    	provider = new CustomAuthenticationProvider(authenticationService);
    	
    	return provider;
    }
    
    
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
       
    }

    @Override
    protected AuthenticationManager authenticationManager() throws Exception {
    	List<AuthenticationProvider> authenticationProviderList = new ArrayList<AuthenticationProvider>();
		authenticationProviderList.add(authenticationProvider());
		return  new ProviderManager(
				authenticationProviderList);
		
    }

    
    //TODO: (Mandar) Remove the websocket endpoint from "permitAll" list.
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .exceptionHandling()                 
                .and()
                .logout().invalidateHttpSession(true)
                .permitAll()
                .logoutUrl(LOGOUT_PATH)
                .logoutSuccessHandler(logoutSuccessHandler)
                .and().anonymous().and().servletApi().and().headers().cacheControl().and().and()
				.authorizeRequests()
				.antMatchers("/").permitAll()
				.antMatchers(RMS_API_URL_V1 + "auth/**").permitAll()
				.antMatchers(RMS_API_URL_V1 + "systeminfo/**").permitAll()
				.antMatchers(RMS_API_URL_V1 + "failover/cluster/**").permitAll()
				.antMatchers(RMS_API_URL_V1 + "failover/state").permitAll()
				.antMatchers(RMS_API_URL_V1 + "firmware/download/**").permitAll()
				.antMatchers(Constants.WS_URL_END_POINT + "/**").permitAll()
				.antMatchers("/agent_api/**").permitAll()
				.antMatchers(RMS_API_URL_V1 + "logcollector/**").permitAll()
				.antMatchers(RMS_API_URL_V1 + "logs/**").permitAll();
				
        
        http.authorizeRequests().anyRequest().authenticated();
       http.addFilterBefore(new StatelessLdapLoginFilter(LOGIN_PATH, tokenAuthenticationService, authenticationManager()),
    		   UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(new StatelessAuthenticationFilter(tokenAuthenticationService), StatelessLdapLoginFilter.class);
    }

    
}
