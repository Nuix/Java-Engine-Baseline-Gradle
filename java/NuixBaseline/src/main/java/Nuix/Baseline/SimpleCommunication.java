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
    DateTime      commDate;
    List<Address> toAddresses;
    List<Address> fromAddresses;
    List<Address> ccAddresses;
    List<Address> bccAddresses;
    List<Address> delegateAddresses;


    /**
     * Holds information about communication data for a single item.
     * @param commDate the date of the communication.
     * @param fromAddresses The senders (From) for the communication.
     * @param toAddresses The direct recipients (To) for the communication.
     * @param ccAddresses The indirect recipients (Cc) for the communication.
     * @param bccAddresses The hidden recipients (Bcc) for the communication.
     * @param delegateAddresses The delegated senders (Sender) for the communication.
     */
    public SimpleCommunication(@Nullable DateTime commDate,
                               @Nonnull ArrayList<Address> fromAddresses,
                               @Nonnull ArrayList<Address> toAddresses,
                               @Nonnull ArrayList<Address> ccAddresses,
                               @Nonnull ArrayList<Address> bccAddresses,
                               @Nonnull ArrayList<Address> delegateAddresses)
    {
        this.commDate          = commDate;
        this.toAddresses       = toAddresses;
        this.fromAddresses     = fromAddresses;
        this.ccAddresses       = ccAddresses;
        this.bccAddresses      = bccAddresses;
        this.delegateAddresses = delegateAddresses;
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
        return commDate;
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
