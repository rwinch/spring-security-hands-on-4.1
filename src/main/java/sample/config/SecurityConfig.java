package sample.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	public void configure(WebSecurity web) throws Exception {
		web
			.ignoring()
				.antMatchers("/polyglot/**");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http
			.authorizeRequests()
				.antMatchers("/resources/**").permitAll()
				.anyRequest().authenticated()
				.and()
			.formLogin()
				.loginPage("/")
				.permitAll()
				.and()
			.httpBasic();
	}
}
