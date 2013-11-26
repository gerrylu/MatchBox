package mobitnt.android.wrapper;

import java.util.ArrayList;
import java.util.List;

import mobitnt.android.data.*;
import mobitnt.util.EADefine;
import mobitnt.util.EAUtil;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.RawContacts;

public class ContactApi {

	static public int CONTACT_FIELD_ID = 0;
	static public int CONTACT_FIELD_NAME = 1;
	static public int CONTACT_FIELD_NUMBER = 2;

	static public int CONTACT_FIELD_COUNT = 3;

	static int m_iContactCount = 0;

	public static final int PHONE_TYPE_HOME = 1;
	public static final int PHONE_TYPE_MOBILE = 2;
	public static final int PHONE_TYPE_WORK = 3;

	public static final int EMAIL_TYPE_HOME = 1;
	public static final int EMAIL_TYPE_WORK = 2;
	public static final int EMAIL_TYPE_OTHER = 3;
	public static final int EMAIL_TYPE_MOBILE = 4;

	static public int GetContactCount() {
		if (m_iContactCount > 0) {
			return m_iContactCount;
		}

		Cursor cur = EAUtil.GetContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		if (cur == null || !cur.moveToFirst()) {
			return 0;
		}
		m_iContactCount = cur.getCount();
		cur.close();
		return m_iContactCount;
	}
	
	static public void InitCache(){
		m_iContactCount = 0;
	}

	static public int RemoveContactByID(int iID) {
		return EADefine.EA_RET_FAILED;
	}

	static public String getContactNameFromPhoneNum(String phoneNum) {
		if (phoneNum == null || phoneNum.length() < 1) {
			return "";
		}

		String contactName = phoneNum;// If contact doesn't exist,then use
										// phoneNo

		ContentResolver cr = EAUtil.GetContentResolver();
		Cursor pCur = cr.query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
				ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?",
				new String[] { phoneNum }, null);

		if (pCur != null && pCur.moveToFirst()) {
			contactName = pCur
					.getString(pCur
							.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

			pCur.close();
		}

		return contactName;
	}

	static public ContactInfo GetContactByID(long lContactID) {
		ContentResolver resolver = EAUtil.GetContentResolver();
		String selection = "";
		String[] selectionArgs = null;
		selection += "_id = ?";
		selectionArgs = new String[] { String.valueOf(lContactID) };

		Cursor cur = resolver.query(ContactsContract.Contacts.CONTENT_URI,
				null, selection, selectionArgs, null);
		if (cur == null || cur.getCount() < 1) {
			if (cur != null) {
				cur.close();
			}
			return null;
		}

		cur.moveToFirst();

		int displayNameColumn = cur
				.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
		ContactInfo contact = new ContactInfo();

		// 获得联系人姓名
		String disPlayName = cur.getString(displayNameColumn);

		contact.lID = lContactID;
		contact.sName = disPlayName;
		contact.sPhone = "";

		cur.close();

		return contact;
	}

	static public List<ContactInfo> GetContactList(int iFrom, int iTo) {
		if (iFrom < 0 || iTo < iFrom) {
			return null;
		}

		ContentResolver resolver = EAUtil.GetContentResolver();
		final String sortOrder = "display_name ASC";
		Cursor cur = resolver.query(ContactsContract.Contacts.CONTENT_URI,
				null, null, null, sortOrder);
		if (cur == null || !cur.moveToFirst()) {
			return null;
		}

		if (false == cur.moveToPosition(iFrom)) {
			cur.close();
			return null;
		}

		int iCurCount = cur.getCount();
		if (iFrom + iCurCount < iTo) {
			iTo = iFrom + iCurCount;
		}

		int iContactCount = iTo - iFrom;

		int idColumn = cur.getColumnIndex(ContactsContract.Contacts._ID);

		int displayNameColumn = cur
				.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);

		List<ContactInfo> clist = new ArrayList<ContactInfo>();

		for (int i = 0; i < iContactCount; ++i) {

			// 获得联系人的ID号
			String contactId = cur.getString(idColumn);
			// 获得联系人姓名
			String disPlayName = cur.getString(displayNameColumn);
			if (disPlayName == null || disPlayName.length() < 1) {
				disPlayName = "";
			}

			/*
			 * if (i == 10){ disPlayName = "aaa"; }
			 */
			// 查看该联系人有多少个电话号码。如果没有这返回值为0
			int phoneCount = cur
					.getInt(cur
							.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
			String sPhones = "";
			if (phoneCount > 0) {
				// 获得联系人的电话号码
				Cursor phones = resolver.query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID
								+ " = " + contactId, null, null);
				if (phones != null && phones.moveToFirst()) {
					do {
						// 遍历所有的电话号码
						// 如果有多个号码这里会有问题！！！
						String phoneNumber = phones
								.getString(phones
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						sPhones += ",";
						sPhones += phoneNumber;
					} while (phones.moveToNext());
				}
			}

			cur.moveToNext();

			if (sPhones.length() < 1) {
				continue;
			}

			String[] sPhoneList = sPhones.split(",");

			if (sPhoneList == null || sPhoneList.length < 1) {
				continue;
			}

			for (int j = 0; j < sPhoneList.length; ++j) {
				if (sPhoneList[j].length() < 3) {
					continue;
				}

				ContactInfo c = new ContactInfo();
				c.lID = Long.parseLong(contactId);
				c.sName = disPlayName;
				c.sPhone = sPhoneList[j];

				c.sName = EAUtil.CHECK_STRING(c.sName, " ");
				c.sPhone = EAUtil.CHECK_STRING(c.sPhone, " ");

				clist.add(c);
			}
		}

		cur.close();
		return clist;
	}

	static public ContactDetailInfo ContactParser(String sID, String sCName,
			String sPhone, String sAddr, String sOrg, String sCMail,
			String sNotes, String sIM) {
		ContactDetailInfo c = new ContactDetailInfo();
		c.lID = Long.parseLong(sID);
		c.sFirstName = sCName;

		if (sPhone != null && sPhone.length() > 0) {
			String[] sPhoneList = sPhone.split(EADefine.ValDelimiter);
			for (int i = 0; i < sPhoneList.length; ++i) {
				String sPhoneItem = sPhoneList[i];
				if (sPhoneItem == null || sPhoneItem.length() <= 1) {
					continue;
				}

				String[] item = sPhoneItem.split(":");
				if (item.length != 2) {
					continue;
				}

				if (c.phoneList == null) {
					c.phoneList = new ArrayList<String>();
				}

				c.phoneList.add(sPhoneItem);
			}
		}

		if (sAddr != null && sAddr.length() > 0) {
			String[] ssAddrList = sAddr.split(EADefine.ValDelimiter);
			for (int i = 0; i < ssAddrList.length; ++i) {
				String sAddrItem = ssAddrList[i];
				if (sAddrItem == null || sAddrItem.length() < 1) {
					continue;
				}

				String[] item = sAddrItem.split(":");
				if (item.length != 2) {
					continue;
				}

				if (c.AddrList == null) {
					c.AddrList = new ArrayList<String>();
				}

				c.AddrList.add(sAddrItem);
			}
		}

		if (sCMail != null && sCMail.length() > 0) {
			String[] sMailList = sCMail.split(EADefine.ValDelimiter);
			for (int i = 0; i < sMailList.length; ++i) {
				String sMailItem = sMailList[i];
				if (sMailItem == null || sMailItem.length() < 1) {
					continue;
				}

				String[] item = sMailItem.split(":");
				if (item.length != 2) {
					continue;
				}

				if (c.EMailList == null) {
					c.EMailList = new ArrayList<String>();
				}

				c.EMailList.add(sMailItem);
			}
		}

		/*
		 * if (sOrg != null && sOrg.length() > 0) {
		 * 
		 * }
		 * 
		 * if (sNotes != null && sNotes.length() > 0) {
		 * 
		 * }
		 * 
		 * if (sIM != null && sIM.length() > 0) {
		 * 
		 * }
		 */

		return c;
	}

	static public ContactDetailInfo GetContactsDetail(long lContactID) {
		String contactId = String.valueOf(lContactID);
		ContentResolver resolver = EAUtil.GetContentResolver();
		String selection = "";
		String[] selectionArgs = null;
		selection += "_id = ?";
		selectionArgs = new String[] { contactId };

		Cursor cur = resolver.query(ContactsContract.Contacts.CONTENT_URI,
				null, selection, selectionArgs, null);
		if (cur == null) {
			return null;
		}

		if (!cur.moveToFirst()) {
			cur.close();
			return null;
		}

		ContactDetailInfo contact = new ContactDetailInfo();
		contact.lID = lContactID;

		int displayNameColumn = cur
				.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);

		// 获得联系人姓名
		contact.sFirstName = cur.getString(displayNameColumn);

		// 查看该联系人有多少个电话号码。如果没有这返回值为0
		int phoneCount = cur.getInt(cur
				.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
		if (phoneCount > 0) {
			// 获得联系人的电话号码
			Cursor phones = resolver.query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "
							+ contactId, null, null);
			if (phones.moveToFirst()) {
				contact.phoneList = new ArrayList<String>();
				do {
					// 遍历所有的电话号码
					String sPhone = phones
							.getString(phones
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					String sType = phones
							.getString(phones
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));

					contact.phoneList.add(sType + ":" + sPhone);

				} while (phones.moveToNext());

				phones.close();
			}
		}

		// 获取该联系人邮箱
		Cursor emails = resolver.query(
				ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "
						+ contactId, null, null);
		if ((emails != null) && (emails.moveToFirst())) {
			contact.EMailList = new ArrayList<String>();

			do {
				// 遍历所有的电话号码
				String emailType = emails
						.getString(emails
								.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));

				String emailValue = emails
						.getString(emails
								.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));

				contact.EMailList.add(emailType + ":" + emailValue);
			} while (emails.moveToNext());

			emails.close();
		}

		// 获取该联系人IM
		Cursor IMs = resolver.query(Data.CONTENT_URI, new String[] { Data._ID,
				Im.PROTOCOL, Im.DATA }, Data.CONTACT_ID + "=?" + " AND "
				+ Data.MIMETYPE + "='" + Im.CONTENT_ITEM_TYPE + "'",
				new String[] { contactId }, null);
		if ((IMs != null) && IMs.moveToFirst()) {
			contact.ImList = new ArrayList<String>();
			do {
				String protocol = IMs
						.getString(IMs.getColumnIndex(Im.PROTOCOL));
				String data = IMs.getString(IMs.getColumnIndex(Im.DATA));

				contact.ImList.add(protocol + ":" + data);

			} while (IMs.moveToNext());
			IMs.close();
		}

		// 获取该联系人地址
		Cursor address = resolver.query(
				ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
				null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "
						+ contactId, null, null);
		if ((address != null) && address.moveToFirst()) {
			contact.AddrList = new ArrayList<String>();
			do {
				// 遍历所有的地址
				String street = address
						.getString(address
								.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
				String city = address
						.getString(address
								.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
				String region = address
						.getString(address
								.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
				String postCode = address
						.getString(address
								.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
				/*
				 * String formatAddress = address .getString(address
				 * .getColumnIndex
				 * (ContactsContract.CommonDataKinds.StructuredPostal
				 * .FORMATTED_ADDRESS));
				 */
				contact.AddrList.add("street:" + street);
				contact.AddrList.add("city:" + city);
				contact.AddrList.add("region:" + region);
				contact.AddrList.add("postCode:" + postCode);
				// contact.AddrList.add("formatAddress:" + formatAddress);
			} while (address.moveToNext());

			address.close();
		}

		// 获取该联系人组织
		Cursor organizations = resolver.query(Data.CONTENT_URI, new String[] {
				Data._ID, Organization.COMPANY, Organization.TITLE },
				Data.CONTACT_ID + "=?" + " AND " + Data.MIMETYPE + "='"
						+ Organization.CONTENT_ITEM_TYPE + "'",
				new String[] { contactId }, null);
		if ((organizations != null) && organizations.moveToFirst()) {
			contact.OrgList = new ArrayList<String>();
			do {
				String company = organizations.getString(organizations
						.getColumnIndex(Organization.COMPANY));
				String title = organizations.getString(organizations
						.getColumnIndex(Organization.TITLE));
				contact.OrgList.add("company:" + company);
				contact.OrgList.add("title:" + title);
			} while (organizations.moveToNext());
			organizations.close();
		}

		// 获取备注信息
		Cursor notes = resolver.query(Data.CONTENT_URI, new String[] {
				Data._ID, Note.NOTE }, Data.CONTACT_ID + "=?" + " AND "
				+ Data.MIMETYPE + "='" + Note.CONTENT_ITEM_TYPE + "'",
				new String[] { contactId }, null);
		if ((notes != null) && notes.moveToFirst()) {
			contact.OrgList = new ArrayList<String>();
			do {
				String noteinfo = notes.getString(notes
						.getColumnIndex(Note.NOTE));
				contact.OrgList.add("noteinfo:" + noteinfo);
			} while (notes.moveToNext());

			notes.close();
		}

		/*
		 * // 获取nickname信息 Cursor nicknames = resolver.query(Data.CONTENT_URI,
		 * new String[] { Data._ID, Nickname.NAME }, Data.CONTACT_ID + "=?" +
		 * " AND " + Data.MIMETYPE + "='" + Nickname.CONTENT_ITEM_TYPE + "'",
		 * new String[] { contactId }, null); if (nicknames.moveToFirst()) { do
		 * { String nickname_ = nicknames.getString(nicknames
		 * .getColumnIndex(Nickname.NAME)); Log.i("nickname_", nickname_); }
		 * while (nicknames.moveToNext()); }
		 */

		return contact;
	}

	/**
	 * 新建联系人的接口
	 * 
	 * @param String
	 *            accountName，accountType 为账号名账号类型，一般为NULL
	 * @throws RemoteException
	 * @throws OperationApplicationException
	 */

	public static boolean insertContact(ContactDetailInfo c) {
		try {
			ContentResolver resolver = EAUtil.GetContentResolver();
			ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

			// insert new record
			ops.add(ContentProviderOperation
					.newInsert(ContactsContract.RawContacts.CONTENT_URI)
					.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
					.withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
					.withValue(
							ContactsContract.RawContacts.AGGREGATION_MODE,
							ContactsContract.RawContacts.AGGREGATION_MODE_DISABLED)
					.build());

			// update name
			ops.add(ContentProviderOperation
					.newInsert(ContactsContract.Data.CONTENT_URI)
					.withValueBackReference(
							ContactsContract.Data.RAW_CONTACT_ID, 0)
					.withValue(
							ContactsContract.Data.MIMETYPE,
							ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
					.withValue(
							ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
							c.sFirstName + " " + c.sLastName).build());

			// update phone
			if (c.phoneList != null) {
				for (int j = 0; j < c.phoneList.size(); j++) {
					String p = c.phoneList.get(j);
					String[] p1 = p.split(":");
					int iType = Integer.parseInt(p1[0]);

					ops.add(ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(
									ContactsContract.Data.RAW_CONTACT_ID, 0)
							.withValue(
									ContactsContract.Data.MIMETYPE,
									ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
							.withValue(
									ContactsContract.CommonDataKinds.Phone.NUMBER,
									p1[1]).withValue(Phone.TYPE, iType).build());
				}
			}

			// update email
			if (c.EMailList != null) {
				for (int j = 0; j < c.EMailList.size(); j++) {
					String p = c.EMailList.get(j);
					String[] p1 = p.split(":");
					if (p1.length != 2){
						continue;
					}
					
					int iType = Integer.parseInt(p1[0]);
					String sEmail = p1[1];
					if (sEmail == ""){
						continue;
					}
					
					ops.add(ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(
									ContactsContract.Data.RAW_CONTACT_ID, 0)
							.withValue(
									ContactsContract.Data.MIMETYPE,
									ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
							.withValue(
									ContactsContract.CommonDataKinds.Email.DATA,
									sEmail)
							.withValue(
									ContactsContract.CommonDataKinds.Email.TYPE,
									iType).build());
				}
			}

			// update address
			if (c.AddrList != null) {
				String street = "", city = "", region = "";
				for (int j = 0; j < c.AddrList.size(); j++) {
					String[] a1 = c.AddrList.get(j).split(":");
					if (a1[0].equals("street")) {
						street = a1[1];
					} else if (a1[0].equals("city")) {
						city = a1[1];
					} else if (a1[0].equals("region")) {
						region = a1[1];
					}
				}

				ops.add(ContentProviderOperation
						.newInsert(ContactsContract.Data.CONTENT_URI)
						.withValueBackReference(
								ContactsContract.Data.RAW_CONTACT_ID, 0)
						.withValue(
								ContactsContract.Data.MIMETYPE,
								ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
						.withValue(
								ContactsContract.CommonDataKinds.StructuredPostal.STREET,
								street)
						.withValue(
								ContactsContract.CommonDataKinds.StructuredPostal.CITY,
								city)
						.withValue(
								ContactsContract.CommonDataKinds.StructuredPostal.REGION,
								region)
						.withValue(
								ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
								ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK)
						.build());
			}

			/*
			 * if (c.ImList != null) { for (int j = 0; j < c.ImList.size(); j++)
			 * { String[] i1 = c.ImList.get(j).split(":"); String[] item = {
			 * i1[1] }; insertItemToContact(ops, i1[0], rawId,
			 * PROJECTION_IM_CONTACT, item); } } if (c.OrgList != null) { for
			 * (int j = 0; j < c.OrgList.size(); j++) { String[] o1 =
			 * c.OrgList.get(j).split(":"); String[] item = { o1[1] };
			 * insertItemToContact(ops, o1[0], rawId,
			 * PROJECTION_ORGANIZATION_CONTACT, item); } } if (c.NotesList !=
			 * null) { for (int j = 0; j < c.NotesList.size(); j++) { String[]
			 * n1 = c.NotesList.get(j).split(":"); String[] item = { n1[1] };
			 * insertItemToContact(ops, n1[0], rawId, PROJECTION_NOTES_CONTACT,
			 * item); } }
			 */

			ContentProviderResult[] cprs = resolver.applyBatch(
					ContactsContract.AUTHORITY, ops);
			for (int i = 0; i < cprs.length; ++i) {
				if (cprs[i].uri == null) {
					return false;
				}
			}

			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * 通过往ROWCONTACT里插入数据，获得rawId
	 * 
	 * @param cr
	 * 
	 * @param accountName 一般为NULL
	 * 
	 * @param accountType 一般为NULL
	 * 
	 * @return
	 */

	/*
	 * static long insertRawContact(ContentResolver cr, String accountName,
	 * String accountType) {
	 * 
	 * ContentValues values = new ContentValues();
	 * values.put(RawContacts.ACCOUNT_NAME, accountName);
	 * values.put(RawContacts.ACCOUNT_TYPE, accountType); //
	 * values.put(Contacts.DISPLAY_NAME, displayName); Uri rawContactUri =
	 * cr.insert(RawContacts.CONTENT_URI, values); long rawContactId =
	 * ContentUris.parseId(rawContactUri); return rawContactId; }
	 */

	/*
	 * static void insertItemToContact(ArrayList<ContentProviderOperation> ops,
	 * String mimeType, String rawContactId, String[] PROJECTION_CONTACT,
	 * String[] item) throws RemoteException, OperationApplicationException { //
	 * ContentValues values = new ContentValues(); //
	 * values.put(Data.RAW_CONTACT_ID, rawContactId); //
	 * values.put(Data.MIMETYPE, mimeType); // for (int i = 0; i <
	 * PROJECTION_CONTACT.length; i++) { // values.put(PROJECTION_CONTACT[i],
	 * item[i]); // } // Uri dataUri = cr.insert(Data.CONTENT_URI, values);
	 * 
	 * Builder builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
	 * builder.withYieldAllowed(true); builder.withValue(Data.RAW_CONTACT_ID,
	 * rawContactId); builder.withValue(Data.MIMETYPE, mimeType); for (int i =
	 * 0; i < PROJECTION_CONTACT.length; i++) {
	 * builder.withValue(PROJECTION_CONTACT[i], item[i]); }
	 * ops.add(builder.build()); }
	 */

	static public boolean delete(long lContactID) {
		if (0 == lContactID) {
			return false;
		}

		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		ops.add(ContentProviderOperation
				.newDelete(
						ContentUris.withAppendedId(RawContacts.CONTENT_URI,
								lContactID)).build());

		try {
			ContentProviderResult[] cprs = EAUtil.GetContentResolver()
					.applyBatch(ContactsContract.AUTHORITY, ops);
			for (int i = 0; i < cprs.length; ++i) {
				if (cprs[i].count == 0) {
					return false;
				}
			}
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	// update is implementd by insert a new contact and then remove the old one
	static public boolean update(ContactDetailInfo c) {
		try {
			if (!insertContact(c)) {
				return false;
			}

			return delete(c.lID);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	@SuppressWarnings("unused")
	private List<ContactInfo>  getKeepedContacts(int iFrom,int iTo) {
		if (iFrom < 0 || iTo < iFrom) {
			return null;
		}

		ContentResolver resolver = EAUtil.GetContentResolver();

		Cursor cur = resolver.query(ContactsContract.Contacts.CONTENT_URI, null,
				ContactsContract.Contacts.STARRED + " =  1 ", null, null);

		if (cur == null || !cur.moveToFirst()) {
			return null;
		}

		if (false == cur.moveToPosition(iFrom)) {
			cur.close();
			return null;
		}

		int iCurCount = cur.getCount();
		if (iFrom + iCurCount < iTo) {
			iTo = iFrom + iCurCount;
		}

		int iContactCount = iTo - iFrom;

		int idColumn = cur.getColumnIndex(ContactsContract.Contacts._ID);

		int displayNameColumn = cur
				.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);

		List<ContactInfo> clist = new ArrayList<ContactInfo>();

		for (int i = 0; i < iContactCount; ++i) {

			// 获得联系人的ID号
			String contactId = cur.getString(idColumn);
			// 获得联系人姓名
			String disPlayName = cur.getString(displayNameColumn);
			if (disPlayName == null || disPlayName.length() < 1) {
				disPlayName = "";
			}

			/*
			 * if (i == 10){ disPlayName = "aaa"; }
			 */
			// 查看该联系人有多少个电话号码。如果没有这返回值为0
			int phoneCount = cur
					.getInt(cur
							.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
			String sPhones = "";
			if (phoneCount > 0) {
				// 获得联系人的电话号码
				Cursor phones = resolver.query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID
								+ " = " + contactId, null, null);
				if (phones != null && phones.moveToFirst()) {
					do {
						// 遍历所有的电话号码
						// 如果有多个号码这里会有问题！！！
						String phoneNumber = phones
								.getString(phones
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						sPhones += ",";
						sPhones += phoneNumber;
					} while (phones.moveToNext());
				}
			}

			cur.moveToNext();

			if (sPhones.length() < 1) {
				continue;
			}

			String[] sPhoneList = sPhones.split(",");

			if (sPhoneList == null || sPhoneList.length < 1) {
				continue;
			}

			for (int j = 0; j < sPhoneList.length; ++j) {
				if (sPhoneList[j].length() < 3) {
					continue;
				}

				ContactInfo c = new ContactInfo();
				c.lID = Long.parseLong(contactId);
				c.sName = disPlayName;
				c.sPhone = sPhoneList[j];

				c.sName = EAUtil.CHECK_STRING(c.sName, " ");
				c.sPhone = EAUtil.CHECK_STRING(c.sPhone, " ");

				clist.add(c);
			}
		}

		cur.close();
		return clist;

	}

}
