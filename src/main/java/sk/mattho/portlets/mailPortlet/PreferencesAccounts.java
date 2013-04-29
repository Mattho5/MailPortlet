package sk.mattho.portlets.mailPortlet;

import java.util.ArrayList;
import java.util.List;

import sk.mattho.portlets.mailPortlet.utils.DESEncryption;

public class PreferencesAccounts {
	private static String ACCOUNT_SEPARATOR= "[[";
	private static String SEPARATOR_REGEX="\\[\\[";
	private List<AccountInfo> accounts;
	private DESEncryption encryptor;
	
	private void init(){
		this.accounts= new ArrayList<AccountInfo>();
		try {
			this.encryptor= new DESEncryption();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public PreferencesAccounts(){
		this.init();
	}
	
	public PreferencesAccounts(String accs){
		this.init();
		for(String accString: accs.split("\\[\\[")){
			this.accounts.add(new AccountInfo(encryptor.decrypt(accString)));
		}
	}
	
	public List<AccountInfo> accounts(){
		return accounts;
	}
	//public void removeAccount(String userName,)
	public String getAccounts() {
		String temp="";
		
		for(AccountInfo a:accounts){
			temp+=encryptor.encrypt(a.toString())+ ACCOUNT_SEPARATOR;
		
		}
		System.out.println(temp);
		return temp;
		
	}
	
	public void addAccount(AccountInfo a){
		this.accounts.add(a);
		}

}
