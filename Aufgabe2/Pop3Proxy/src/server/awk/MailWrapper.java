package server.awk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class MailWrapper {
	private final File mail;
	private boolean deleted = false;
	private int currentIndex = -1;
	
	public MailWrapper(File mail) throws IOException, NoSuchAlgorithmException {
		super();
		this.mail = mail;
        
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
	

	public String getMd5hash() {
		System.out.println(mail.getName());
		return mail.getName().split("\\.")[0];
	}

//	private String readFile( File file ) throws IOException {
//		BufferedReader reader = new BufferedReader( new FileReader (file));
//	    String         line = null;
//	    StringBuilder  stringBuilder = new StringBuilder();
//	    String         ls = "\n";
//	
//	    while( ( line = reader.readLine() ) != null ) {
//	        stringBuilder.append( line );
//	        stringBuilder.append( ls );
//	    }
//	
//	    return stringBuilder.toString();
//	}
//	public List<String> getSplitedMail(){
//		List<String> splitetMail = new ArrayList<>();
//		for(int i=0; i < (mailContent.length()/510) + 1; i++){
//			int end = (((i*510) + 510)) > (mailContent.length()-1) ? (mailContent.length()-1) : ((i*510) + 510);
//			splitetMail.add(mailContent.substring(i*510, end));
//		}
//		return splitetMail;
//	}
	public String next() throws IOException{
		String retVal = null;
		if(currentIndex < (mail.length()/510)){
			currentIndex++;
			RandomAccessFile file = new RandomAccessFile(mail, "rw");
			//int readEnd = (int) ((currentIndex*510)+510 > mail.length() ? mail.length() : (currentIndex*510)+510);
			file.seek(currentIndex * 510);
			int anz = 0;
			int readChar = - 1;
			while(anz < 510 && ((readChar = file.read()) > -1 )){
				retVal += "" +(char) readChar;
			}
			System.out.println(currentIndex);
			file.close();
		}
		return retVal;
	}
	
	public void reset(){
		currentIndex = -1;
	}
}
