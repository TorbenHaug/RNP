package server.awk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class MailWrapper {
	private final File mail;
	private final String mailContent;
	private final String md5hash;
	private boolean deleted = false;
	
	public MailWrapper(File mail) throws IOException, NoSuchAlgorithmException {
		super();
		this.mail = mail;
		this.mailContent = readFile(mail);
		System.out.println(mailContent);
        this.md5hash = md5Hash(mailContent);
        
	}
	
	public File getMail(){
		return mail;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
	public String getMailContent() {
		return mailContent;
	}

	public String getMd5hash() {
		return md5hash;
	}
	
	private String md5Hash(String message) throws NoSuchAlgorithmException, UnsupportedEncodingException{
		MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] hashedBytes = digest.digest(message.getBytes("UTF-8"));
        
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < hashedBytes.length; i++) {
            stringBuffer.append(Integer.toString((hashedBytes[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return stringBuffer.toString();
	}

	private String readFile( File file ) throws IOException {
		BufferedReader reader = new BufferedReader( new FileReader (file));
	    String         line = null;
	    StringBuilder  stringBuilder = new StringBuilder();
	    String         ls = "\n";
	
	    while( ( line = reader.readLine() ) != null ) {
	        stringBuilder.append( line );
	        stringBuilder.append( ls );
	    }
	
	    return stringBuilder.toString();
	}
	public List<String> getSplitedMail(){
		List<String> splitetMail = new ArrayList<>();
		for(int i=0; i < (mailContent.length()/510) + 1; i++){
			int end = (((i*510) + 510)) > (mailContent.length()-1) ? (mailContent.length()-1) : ((i*510) + 510);
			splitetMail.add(mailContent.substring(i*510, end));
		}
		return splitetMail;
	}
}
