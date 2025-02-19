
package com.simplilearn.GMailAPI;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Set;
import com.google.api.services.gmail.model.Message;

import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.io.ByteArrayOutputStream;
import org.apache.commons.codec.binary.Base64;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;

public class Gmailer {
	
	private static final String TEST_EMAIL = "noshaymaano@gmail.com";
	private Gmail service;
	private RecipientType TO;
	
	public Gmailer() throws Exception {
		 NetHttpTransport HttpTransport = GoogleNetHttpTransport.newTrustedTransport();
		 GsonFactory jsonFactory = GsonFactory.getDefaultInstance();
		 service = new Gmail.Builder(HttpTransport, jsonFactory, getCredentials(HttpTransport, jsonFactory))
			        .setApplicationName("test Mailer")
			        .build();
		
		
	}
	
	 private static Credential getCredentials(final NetHttpTransport httpTransport, GsonFactory jsonFactory)
		      throws IOException {
		    // Load client secrets.
	
		    GoogleClientSecrets clientSecrets =
		        GoogleClientSecrets.load(jsonFactory, new InputStreamReader( Gmailer.class.getResourceAsStream("/Desktop/Project/workspace-java/Meeting-Mate/src/resources/client_secret_46827845441-1ggj6up9443qgablqpie8nqe0frd18tl.apps.googleusercontent.com.json")));

		    // Build flow and trigger user authorization request.
		    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
		    			httpTransport, jsonFactory, clientSecrets, Set.of(GmailScopes.GMAIL_SEND))
		        .setDataStoreFactory(new FileDataStoreFactory(Paths.get("tokens").toFile()))
		        .setAccessType("offline")
		        .build();
		    
		    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		    return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
		    
		  }

	
	public void sendMail(String subject, String message) throws Exception {
		
		    // Encode as MIME message
		    
		    Properties props = new Properties();
		    Session session = Session.getDefaultInstance(props, null);
		    MimeMessage email = new MimeMessage(session);
		    email.setFrom(new InternetAddress(TEST_EMAIL));
		    email.addRecipient(TO, new InternetAddress(TEST_EMAIL));
		    email.setSubject(subject);
		    email.setText(message);
		    
		    // Encode and wrap the MIME message into a gmail message
		    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		    email.writeTo(buffer);
		    byte[] rawMessageBytes = buffer.toByteArray();
		    String encodedEmail = Base64.encodeBase64URLSafeString(rawMessageBytes);
		    Message msg = new Message();
		    msg.setRaw(encodedEmail);

		    try {
		      // Create send message
		      msg = service.users().messages().send("me", msg).execute();
		      System.out.println("Message id: " + msg.getId());
		      System.out.println(msg.toPrettyString());
		      
		    } catch (GoogleJsonResponseException e) {
		      // TODO(developer) - handle error appropriately
		      GoogleJsonError error = e.getDetails();
		      if (error.getCode() == 403) {
		        System.err.println("Unable to send message: " + e.getDetails());
		      } else {
		        throw e;
		      }
		    }


	}

	public static void main(String[] args) throws Exception {

		new Gmailer().sendMail("A new Message", """
				Dear reader,
				
				Hello World.
				
				Best Regards,
				myself
				
				""");
	}

}
