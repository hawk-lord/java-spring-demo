package ax.joint.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class HelloControllerTest {

    private static final int LOG_ROUNDS = 10;

    @Autowired
    private MockMvc mvc;

    /**
     * Should return a nonempty string.
     * @throws Exception
     */
    @Test
    public void getString() throws Exception {
        this.mvc.perform(get("/string")
                .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(isEmptyOrNullString())));
    }


    /**
     * Should return NOK since no string has been created with GET.
     * @throws Exception
     */
    @Test
    public void postString() throws Exception {
        this.mvc.perform(post("/string")
                .param("id", "2")
                .param("string", "abcdefghij"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(is(equalTo("NOK"))));
    }


    /**
     * Should return OK, since the encrypted strings and ids are the same.
     *
     * @throws Exception
     */
    @Test
    public void getAndPostStringOk() throws Exception {
        final ResultActions resultActions = this.mvc.perform(get("/string")
                .param("id", "3"))
                .andExpect(status().isOk())
                .andExpect(content().string(is(not(equalTo("")))));
        final MvcResult result = resultActions.andReturn();
        final String string = result.getResponse().getContentAsString();
        final String encryptedString = BCrypt.hashpw(string, BCrypt.gensalt(LOG_ROUNDS));
        this.mvc.perform(post("/string")
                .param("id", "3")
                .param("string", encryptedString))
                .andExpect(status().isOk())
                .andExpect(content().string(is(equalTo("OK"))));
    }

    /**
     * Should return NOK since no object with the same ID has been created.
     *
     * @throws Exception
     */
    @Test
    public void getAndPostStringWrongId() throws Exception {
        final ResultActions resultActions = this.mvc.perform(get("/string")
                .param("id", "4"))
                .andExpect(status().isOk())
                .andExpect(content().string(is(not(equalTo("")))));
        final MvcResult result = resultActions.andReturn();
        final String string = result.getResponse().getContentAsString();
        final String encryptedString = BCrypt.hashpw(string, BCrypt.gensalt(LOG_ROUNDS));
        this.mvc.perform(post("/string")
                .param("id", "5")
                .param("string", encryptedString))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(is(equalTo("NOK"))));
    }

    /**
     * Should return NOK since the IDs are same but the strings are different.
     *
     * @throws Exception
     */
    @Test
    public void getAndPostStringWrongString() throws Exception {
        final ResultActions resultActions = this.mvc.perform(get("/string")
                .param("id", "6"))
                .andExpect(status().isOk())
                .andExpect(content().string(is(not(equalTo("")))));
        final MvcResult result = resultActions.andReturn();
        final String string = result.getResponse().getContentAsString();
        final String encryptedString = BCrypt.hashpw(string, BCrypt.gensalt(LOG_ROUNDS));
        this.mvc.perform(post("/string")
                .param("id", "6")
                .param("string", string))
                .andExpect(status().is5xxServerError())
                .andExpect(content().string(is(equalTo("NOK"))));
    }


}
