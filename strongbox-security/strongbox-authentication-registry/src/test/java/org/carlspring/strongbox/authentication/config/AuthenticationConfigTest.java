package org.carlspring.strongbox.authentication.config;

import org.carlspring.strongbox.authentication.TestConfig;
import org.carlspring.strongbox.authentication.api.Authenticator;
import org.carlspring.strongbox.authentication.api.impl.xml.PasswordAuthenticator;
import org.carlspring.strongbox.authentication.registry.AuthenticatorsRegistry;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import org.hamcrest.CoreMatchers;
import org.hamcrest.CustomMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertThat;

/**
 * @author Przemyslaw Fusik
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = TestConfig.class)
public class AuthenticationConfigTest
{

    @Inject
    AuthenticatorsRegistry authenticatorsRegistry;

    @Test
    public void registryShouldNotBeNull()
    {
        assertThat(authenticatorsRegistry, CoreMatchers.notNullValue());
    }

    @Test
    public void registryShouldContainStrongboxBuiltinAuthenticator()
    {
        assertThat(Lists.newArrayList(authenticatorsRegistry), CoreMatchers.hasItem(
                new CustomMatcher<Authenticator>("registryShouldContainStrongboxBuiltinAuthenticator")
                {
                    @Override
                    public boolean matches(Object o)
                    {
                        return ((Authenticator) o).getName()
                                                  .equals(
                                                          PasswordAuthenticator.class.getName());
                    }
                }));
    }

    @Test
    public void registryShouldContainEmptyAuthenticator()
    {
        assertThat(Lists.newArrayList(authenticatorsRegistry),
                   CoreMatchers.hasItem(new CustomMatcher<Authenticator>("registryShouldContainEmptyAuthenticator")
                   {
                       @Override
                       public boolean matches(Object o)
                       {
                           return ((Authenticator) o).getName()
                                                     .equals("org.carlspring.strongbox.authentication.impl.example.EmptyAuthenticator");
                       }
                   }));
    }

    @Test
    public void registryShouldContainLdapAuthenticator()
    {
        assertThat(Lists.newArrayList(authenticatorsRegistry),
                   CoreMatchers.hasItem(new CustomMatcher<Authenticator>("registryShouldContainLdapAuthenticator")
                   {
                       @Override
                       public boolean matches(Object o)
                       {
                           return ((Authenticator) o).getName()
                                                     .equals("org.carlspring.strongbox.authentication.api.impl.ldap.LdapAuthenticator");
                       }
                   }));
    }

}
