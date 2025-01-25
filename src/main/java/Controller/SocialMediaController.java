package Controller;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import Model.Account;
import Model.Message;
import Service.AccountService;
import Service.MessageService;
import Service.UnauthorizedException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;


/**
 * TODO: You will need to write your own endpoints and handlers for your controller. The endpoints you will need can be
 * found in readme.md as well as the test cases. You should
 * refer to prior mini-project labs and lecture materials for guidance on how a controller may be built.
 */
public class SocialMediaController {
    /**
     * In order for the test cases to work, you will need to write the endpoints in the startAPI() method, as the test
     * suite must receive a Javalin object from this method.
     * @return a Javalin app object which defines the behavior of the Javalin controller.
     */
    private AccountService accountService;
    private MessageService messageService;

    public SocialMediaController(){
        this.accountService=new AccountService();
        this.messageService = new MessageService();
    }

    public Javalin startAPI() {
        Javalin app = Javalin.create();
        app.get("example-endpoint", this::exampleHandler);
        app.post("/register", this::registerAccount);
        app.post("/login", this::loginAccount);
        app.post("/messages", this::createMessage);
        app.get("/messages", this::getAllMessages);
        app.get("/messages/{message_id}", this::getMessageById);
        app.delete("/messages/{message_id}", this::deleteMessageById);
        app.patch("/messages/{message_id}", this::updateMessageById);
        app.get("/accounts/{account_id}/messages",
                this::getMessagesByAccountId);

       

        return app;
    }

    /**
     * This is an example handler for an example endpoint.
     * @param context The Javalin Context object manages information about both the HTTP request and response.
     */
    private void exampleHandler(Context context) {
        context.json("sample text");
    }


    /**
     * This method handles the registration process for new users.
     * It expects a POST request to "/register" with the new account details in the
     * request body.
     */
    private void registerAccount(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Account account = mapper.readValue(ctx.body(), Account.class);
        try {
            Account registeredAccount = accountService.createAccount(account);

            // Send the registered account as a JSON response
            ctx.json(mapper.writeValueAsString(registeredAccount));
        } catch (UnauthorizedException e) {
            // Set the response status to 400 (Bad Request) in case of exception
            ctx.status(400);
        }
    }

    /**
     * This method handles the login process for users.
     * It expects a POST request to "/login" with the account credentials in the
     * request body.
     */
    private void loginAccount(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper(); // it calls a default no-arg constructor from Model.Account - REQUIRED
                                                  // for Jackson ObjectMapper
        Account account = mapper.readValue(ctx.body(), Account.class);

        try {
            Optional<Account> loggedInAccount = accountService
                    .validateLogin(account);
            if (loggedInAccount.isPresent()) {
               
                ctx.json(mapper.writeValueAsString(loggedInAccount));
                ctx.sessionAttribute("logged_in_account",
                        loggedInAccount.get());
                ctx.json(loggedInAccount.get());
            } else {
                
                ctx.status(401);
            }
        } catch (UnauthorizedException e) {
            
            ctx.status(401);
        }
    }

    /**
     * This method handles the creation of new messages.
     * It expects a POST request to "/messages" with the message details in the
     * request body.
     */
    private void createMessage(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Message mappedMessage = mapper.readValue(ctx.body(), Message.class);
        try {
            Optional<Account> account = accountService
                    .getAccountById(mappedMessage.getPosted_by());
            Message message = messageService.createMessage(mappedMessage,
                    account);
            ctx.json(message);
        } catch (UnauthorizedException e) {
            
            ctx.status(400);
        }
    }

    /**
     * This method retrieves all messages.
     * It expects a GET request to "/messages".
     */
    private void getAllMessages(Context ctx) {

        List<Message> messages = messageService.getAllMessages();
        ctx.json(messages);
    }

    /**
     * This method handles the retrieval of a specific message by its ID.
     * It expects a GET request to "/messages/{message_id}".
     */

    private void getMessageById(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("message_id"));
            Optional<Message> message = messageService.getMessageById(id);
            if (message.isPresent()) {
                ctx.json(message.get());
            } else {
                
                ctx.status(200); 
                ctx.result(""); 
            }
            
        } catch (NumberFormatException e) {
            ctx.status(400); 
        } catch (UnauthorizedException e) {
            ctx.status(200); 
            ctx.result("");
        }
    }

    /**
     * This method handles the deletion of a specific message by its ID.
     * It expects a DELETE request to "/messages/{message_id}".
     */
    private void deleteMessageById(Context ctx) {
        try {
           
            int id = Integer.parseInt(ctx.pathParam("message_id"));

            // Attempt to retrieve the message by its ID
            Optional<Message> message = messageService.getMessageById(id);
            if (message.isPresent()) {
              
                messageService.deleteMessage(message.get());
                ctx.status(200);
               
                ctx.json(message.get());
            } else {
               
                ctx.status(200);
            }
        } catch (UnauthorizedException e) {
            
            ctx.status(200);
        }
    }

    /**
     * This method handles the update of a specific message by its ID.
     * It expects a PATCH request to "/messages/{message_id}" with the new content
     * of the message in the request body.
     */
    private void updateMessageById(Context ctx) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Message mappedMessage = mapper.readValue(ctx.body(), Message.class);
        try {
            int id = Integer.parseInt(ctx.pathParam("message_id"));
            mappedMessage.setMessage_id(id);
            Message messageUpdated = messageService
                    .updateMessage(mappedMessage);
           
            ctx.json(messageUpdated);

        } catch (UnauthorizedException e) {
            
            ctx.status(400);
        }
    }

    /**
     * This method retrieves all messages associated with a specific account ID.
     * It expects a GET request to "/accounts/{account_id}/messages".
     */
    private void getMessagesByAccountId(Context ctx) {
        try {
            int accountId = Integer.parseInt(ctx.pathParam("account_id"));

            List<Message> messages = messageService
                    .getMessagesByAccountId(accountId);
            if (!messages.isEmpty()) {
               
                ctx.json(messages);
            } else {
                
                ctx.json(messages);
                ctx.status(200);
            }
        } catch (UnauthorizedException e) {
            
            ctx.status(400);
        }
    }

}