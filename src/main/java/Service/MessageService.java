package Service;

import java.util.List;
import java.util.Optional;


import DAO.DAOException;
import DAO.MessageDAO;
import Model.Account;
import Model.Message;
import io.javalin.http.NotFoundResponse;

public class MessageService {
    private MessageDAO messageDao;

    private static final String DB_ACCESS_ERROR_MSG = "Error accessing the database";

    
    public MessageService() {
        messageDao = new MessageDAO();
    }


    public MessageService(MessageDAO messageDao) {
        this.messageDao = messageDao;
    }
    
    public Optional<Message> getMessageById(int id) {

        try {
            Optional<Message> message = messageDao.getById(id);
            if (!message.isPresent()) {
                throw new UnauthorizedException("Message not found");
            }
 
            return message;
        } catch (DAOException e) {
            throw new UnauthorizedException(DB_ACCESS_ERROR_MSG, e);
        }
    }
     
    public List<Message> getAllMessages() {
  
        try {
            List<Message> messages = messageDao.getAll();
   
            return messages;
        } catch (DAOException e) {
            throw new UnauthorizedException(DB_ACCESS_ERROR_MSG, e);
        }
    }

     
    public List<Message> getMessagesByAccountId(int accountId) {
   
        try {
            List<Message> messages = messageDao.getMessagesByAccountId(accountId);
            return messages;

        } catch (DAOException e) {
            throw new UnauthorizedException(DB_ACCESS_ERROR_MSG, e);
        }
    }

    
    public Message createMessage(Message message, Optional<Account> account) {
    
        if (!account.isPresent()) {
            throw new UnauthorizedException("Account must exist when posting a new message");
        }      
        validateMessage(message);       
        checkAccountPermission(account.get(), message.getPosted_by());
        try {
            
            Message createdMessage = messageDao.insert(message);
            return createdMessage;
        } catch (DAOException e) {
            throw new UnauthorizedException(DB_ACCESS_ERROR_MSG, e);
        }
    }
    
    public Message updateMessage(Message message) {
    

        Optional<Message> retrievedMessage = this.getMessageById(message.getMessage_id());

        if (!retrievedMessage.isPresent()) {
            throw new UnauthorizedException("Message not found");
        }
        retrievedMessage.get().setMessage_text(message.getMessage_text());
        validateMessage(retrievedMessage.get());

        try {
           
            messageDao.update(retrievedMessage.get());
            return retrievedMessage.get();
        } catch (DAOException e) {
            throw new UnauthorizedException(DB_ACCESS_ERROR_MSG, e);
        }
    }
   
    public void deleteMessage(Message message) {
   
        try {
            boolean hasDeletedMessage = messageDao.delete(message);
            if (hasDeletedMessage) {
  
            } else {
                throw new NotFoundResponse("Message to delete not found");
            }
        } catch (DAOException e) {
            throw new UnauthorizedException(DB_ACCESS_ERROR_MSG, e);
        }
    }
    
    private void validateMessage(Message message) {
    
        if (message.getMessage_text() == null || message.getMessage_text().trim().isEmpty()) {
            throw new UnauthorizedException("Message text cannot be null or empty");
        }
        if (message.getMessage_text().length() > 254) {
            throw new UnauthorizedException("Message text cannot exceed 254 characters");
        }
    }
    
    private void checkAccountPermission(Account account, int postedBy) {
    
        if (account.getAccount_id() != postedBy) {
            throw new UnauthorizedException("Account not authorized to modify this message");
        }
    }
}
