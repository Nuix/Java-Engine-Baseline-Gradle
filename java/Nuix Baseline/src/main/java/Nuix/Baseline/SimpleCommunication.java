package Nuix.Baseline;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;
import nuix.Address;
import nuix.Communication;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Holds information about communication data for a single item.
 */
public class SimpleCommunication implements Communication{
    DateTime      dateTime;
    List<Address> toAddresses;
    List<Address> fromAddresses;
    List<Address> ccAddresses;
    List<Address> bccAddresses;
    List<Address> delegateAddresses;


    /**
     * Holds information about communication data for a single item.
     * @param myDateTime the date of the communication.
     * @param myFromAddresses The senders (From) for the communication.
     * @param myToAddresses The direct recipients (To) for the communication.
     * @param myCcAddresses The indirect recipients (Cc) for the communication.
     * @param myBccAddresses The hidden recipients (Bcc) for the communication.
     * @param myDelegateAddresses The delegated senders (Sender) for the communication.
     */
    public SimpleCommunication(@Nullable DateTime myDateTime,
                               @Nonnull ArrayList<Address> myFromAddresses,
                               @Nonnull ArrayList<Address> myToAddresses,
                               @Nonnull ArrayList<Address> myCcAddresses,
                               @Nonnull ArrayList<Address> myBccAddresses,
                               @Nonnull ArrayList<Address> myDelegateAddresses)
    {
        dateTime          = myDateTime;
        toAddresses       = myToAddresses;
        fromAddresses     = myFromAddresses;
        ccAddresses       = myCcAddresses;
        bccAddresses      = myBccAddresses;
        delegateAddresses = myDelegateAddresses;
    }

    /**
     * The direct recipients (To) for the communication.
     * @return the direct recipients.
     */
    @Nonnull
    @Override
    public List<Address> getBcc()
    {
        return bccAddresses;
    }

    /**
     * The indirect recipients (Cc) for the communication.
     * @return the indirect recipients.
     */
    @Nonnull
    @Override
    public List<Address> getCc()
    {
        return ccAddresses;
    }

    /**
     * Gets the date of the communication.
     * @return the date of the communication.
     */
    @Override
    public DateTime getDateTime()
    {
        return dateTime;
    }

    /**
     * The senders (From) for the communication. Generally there will be only one, but it is possible for there to be more than one.
     * @return the list of senders.
     */
    @Nonnull
    @Override
    public List<Address> getFrom()
    {
        return fromAddresses;
    }

    /**
     * The delegated senders (Sender) for the communication. Generally there will be only one, but it is possible for there to be more than one.
     * @return the list of delegated senders.
     */
    @Nonnull
    @Override
    public List<Address> getDelegates() {
        return delegateAddresses;
    }

    /**
     * The direct recipients (To) for the communication.
     * @return the direct recipients.
     */
    @Nonnull
    @Override
    public List<Address> getTo()
    {
        return toAddresses;
    }
}
