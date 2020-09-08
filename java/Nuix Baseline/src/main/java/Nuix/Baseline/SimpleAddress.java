package Nuix.Baseline;

import nuix.Address;

/**
 * Represents the address of a single party in a communication, assumed internet-mail type
 */
public class SimpleAddress implements Address {


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
     * @param myPersonal the aesthetic component of the address
     * @param myAddress the actual address
     * @param myType one of the available CommunicationType ("internet-mail,"phone","instant-message")
     */
    public SimpleAddress(String myPersonal, String myAddress, CommunicationType myType)
    {
        //initiator
        personal= myPersonal;
        address = myAddress;
        type    = myType.getValue();
    }

    /**
     * Represents the address of a single party in a communication, assumed internet-mail type
     * @param myPersonal the aesthetic component of the address
     * @param myAddress the actual address
     */
    public SimpleAddress(String myPersonal,String myAddress)
    {
        //initiator (no type is assumed mail)
        personal= myPersonal;
        address = myAddress;
        type    = CommunicationType.COMMUNICATION_MAIL.getValue();
    }

    /**
     * Compares with another address for equality.
     * @param mySimpleAddress the other address.
     * @return true if the other object is the same address, false otherwise.
     */
    @Override
    public boolean equals(Address mySimpleAddress)
    {
        return mySimpleAddress.getAddress().equals(address);
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
     * Lazily implemented instead of full rfc822
     * @return personal if found, otherwise address is returned
     */
    @Override
    public String toRfc822String()
    {
        if (personal == null)
        {
            return address;
        }
        else
        {
            return personal;
        }
    }
}
