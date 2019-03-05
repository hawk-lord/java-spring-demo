package ax.joint.demo;

import java.time.LocalDateTime;

/**
 * Stores strings and information.
 * Only accessors, so the objects are unmodifiable.
 */
class StringTime{

    private final int id;
    private final String string;
    private final String encryptedString;
    private final LocalDateTime localDateTime;

    public StringTime (final int id, final String string, final String encryptedString,
                       final LocalDateTime localDateTime) {
        this.id = id;
        this.string = string;
        this.encryptedString = encryptedString;
        this.localDateTime = localDateTime;
    }

    /**
     *
     *
     * @return User supplied ID to keep track of the object.
     */
    public int getId() {
        return id;
    }

    /**
     *
     *
     * @return original string, created by the program.
     */
    public String getString() {
        return string;
    }

    /**
     *
     * @return encrypted string, to be checked with the user supplied string.
     */
    public String getEncryptedString() {
        return encryptedString;
    }

    /**
     *
     * @return the moment the object was created.
     */
    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    /**
     *
     * @return nicely formatted contents, useful when debugging.
     */
    @Override
    public String toString() {
        return String.format("%s %s %s %s", id, string, encryptedString, localDateTime);
    }

}
