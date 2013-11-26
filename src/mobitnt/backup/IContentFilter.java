/**
 * 
 */
package mobitnt.backup;

/**
 * @author Administratorss
 *
 */
public interface IContentFilter {
	
	public static final int OP_BACKUP_SMS = 0;
	public static final int OP_BACKUP_CALL = 1;
	public static final int OP_RESTORE_SMS = 2;
	public static final int OP_RESTORE_CALL = 3;
	public static final int OP_GET_BACKUP_SMS = 4;
	public static final int OP_GET_BACKUP_CALL = 5;
	
	// Parse mail content
	int ParseMailContent(String sSubject, String sContent) throws Exception;
	

}
