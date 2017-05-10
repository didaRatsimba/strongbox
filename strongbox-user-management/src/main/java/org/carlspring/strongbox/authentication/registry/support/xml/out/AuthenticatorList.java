package org.carlspring.strongbox.authentication.registry.support.xml.out;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AuthenticatorList
{

    @XmlElement(name = "authenticator")
    private List<AuthenticatorListElement> elements = new ArrayList<>();

    public void addElement(AuthenticatorListElement element) {
        elements.add(element);
    }

}
