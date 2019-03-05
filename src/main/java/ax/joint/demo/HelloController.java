package ax.joint.demo;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
public class HelloController {

    /**
     * Seconds before start checking.
     */
    private static final long INITIAL_DELAY = 10L;

    /**
     * Seconds between checks.
     */
    private static final long PERIOD_CHECK = 10L;

    /**
     * String older than this number of seconds will be removed.
     */
    private static final long PERIOD_REMOVE_STRING = 900L;

    /**
     * Salt encryption.
     */
    private static final int LOG_ROUNDS = 10;

    private static final int MIN_LENGTH_INCLUSIVE = 8;

    private static final int MAX_LENGTH_EXCLUSIVE = 33;

    /**
     * Collection of sent strings.
     * Linked list since we will remove from it.
     */
    private final List<StringTime> strings = new LinkedList<>();

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    /**
     * Remove strings if enough time has passed.
     */
    private final Runnable runnable = () -> {
        strings.removeIf(stringTime ->
                stringTime.getLocalDateTime().isBefore(LocalDateTime.now().minusSeconds(PERIOD_REMOVE_STRING)));
    };


    /**
     * Initialise the executor service that will remove old strings.
     */
    public HelloController() {
        scheduledExecutorService.scheduleAtFixedRate(runnable , INITIAL_DELAY, PERIOD_CHECK , TimeUnit.SECONDS );
    }


    /**
     * Creates a random string, encrypts it, and stores it with the request id and a timestamp;
     *
     * @param id to identify the user request.
     * @return the random unencrypted string.
     */
    @GetMapping("/string")
    public ResponseEntity<String> getString(@RequestParam("id") final int id) {
        final String string = RandomStringUtils.randomAlphabetic(MIN_LENGTH_INCLUSIVE, MAX_LENGTH_EXCLUSIVE);
        final String encryptedString = BCrypt.hashpw(string, BCrypt.gensalt(LOG_ROUNDS));
        final StringTime stringTime = new StringTime(id, string, encryptedString, LocalDateTime.now());
        strings.add(stringTime);
        return ResponseEntity.ok(string);
    }

    /**
     *
     * Checks if there is a stored string with the same id.
     * If so, remove the string and return OK.
     * If not, return NOK.
     *
     * If there are no saved strings at all, return NOK.
     *
     * @param id the request id that created the string.
     * @param aString the encrypted string to be checked with the previously stored string.
     * @return OK if OK, NOK otherwise.
     */
    @PostMapping("/string")
    public ResponseEntity<String> postString(@RequestParam("id") final int id,
                                             @RequestParam("string") final String aString) {

        if (strings.size() == 0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("NOK");
        }

        StringTime stringTime = null;
        for (Iterator<StringTime> it = strings.iterator(); it.hasNext();) {
            final StringTime tmpStringTime = it.next();
            if (id == tmpStringTime.getId()) {
                it.remove();
                stringTime = tmpStringTime;
                break;
            }
        }

        if (stringTime == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("NOK");
        }
        try {
            final String result = BCrypt.checkpw(stringTime.getString(), aString) ? "OK" : "NOK";
            if ("OK".equals(result))
                return ResponseEntity.status(HttpStatus.OK).body("OK");
            else
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("NOK");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("NOK");
        }
    }



}