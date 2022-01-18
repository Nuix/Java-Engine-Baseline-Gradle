package Nuix.Baseline;

import nuix.Address;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents the address of a single party in a communication, assumed internet-mail type
 */
public class SimpleAddress implements Address {

    private static final String[] ADDRESS_SPECIALS = "()<>,;:\\\"\t []/".split("");
    private static final String[] PERSONAL_DISPLAY_SPECIALS = "<>@,\\\"".split("");
    String personal;
    String address;
    String type;

    /**
     * The type of the address, either "internet-mail", "phone" or "instant-message".
     */
    public enum CommunicationType {
        COMMUNICATION_MAIL("internet-mail"),COMMUNICATION_PHONE("phone"),COMMUNICATION_MESSAGE("instant-message");

        CommunicationType(String s) {
            value=s;
        }
        private final String value;
        String getValue() {
            return value;
        }
    }

    /**
     * Represents the address of a single party in a communication, assumed internet-mail type
     * @param personal the aesthetic component of the address
     * @param address the actual address
     * @param typeOfCommunication one of the available CommunicationType ("internet-mail,"phone","instant-message")
     */
    public SimpleAddress(@Nullable String personal, @Nonnull String address, @Nonnull CommunicationType typeOfCommunication)
    {
        //initiator
        this.personal= StringUtils.trimToNull(personal);
        this.address = address.trim();
        this.type    = typeOfCommunication.getValue();
    }

    /**
     * Compares with another address for equality.
     * @param address the other address.
     * @return true if the other object is the same address, false otherwise.
     */
    @Override
    public boolean equals(Address address)
    {
        return address.getAddress().toLowerCase().equals(this.address.toLowerCase());
    }

    /**
     * Gets the address part of the address, in a form users can read.
     * @return the address part of the address
     */
    @Override
    public String getAddress()
    {
        return address;
    }

    /**
     * Gets the personal part (the name) of the address, in a form users can read.
     * @return the personal part of the address.
     */
    @Override
    public String getPersonal()
    {
        return personal;
    }

    /**
     * Gets the type of the address, in a form users can read.
     * @return the type of the address, either "internet-mail", "phone" or "instant-message".
     */
    @Override
    public String getType()
    {
        return type;
    }

    /**
     * Gets a string representation of the address, in a form users can read.
     * @return the string representation of the address, in a form users can read.
     */
    @Override
    public String toDisplayString()
    {
        return address;
    }

    /**
     * Lazily implemented instead of full rfc822, missing features are encoding (non-ASCII) and lengths
     * @return personal if found, otherwise address is returned
     */
    @Override
    public String toRfc822String()
    {
        if (personal == null)
        {
            return quoteString(address,ADDRESS_SPECIALS);
        }
        else
        {
            return String.format("%s <%s>",
                    quoteString(personal,PERSONAL_DISPLAY_SPECIALS),
                    quoteString(address,ADDRESS_SPECIALS));
        }
    }

    /**
     * Return the rfc822 string, fallback for logging purposes if that was needed
     * @return the rfc822 string
     */
    @Override
    public String toString()
    {
        return toRfc822String();
    }

    /**
     * Removes the specials passed from the input and returns a quoted string
     * TODO: This could instead encode the result if detected...
     * @param input a string to be cleaned and then quoted
     * @param specials the special characters to omit
     * @return a quoted string that will have specials removed.
     */
    private String quoteString(String input,String[] specials)
    {

        for(String thisChar :specials)
        {
            input=input.replaceAll(thisChar,"");
        }
        return "\"" + input + "\"";
    }
}
