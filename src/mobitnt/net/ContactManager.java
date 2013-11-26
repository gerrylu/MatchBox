package mobitnt.net;

import java.util.List;
import java.util.Properties;

import mobitnt.android.data.ContactDetailInfo;
import mobitnt.android.data.ContactInfo;
import mobitnt.android.wrapper.ContactApi;
import mobitnt.util.*;


public class ContactManager extends PageGen {
	/*
	 <ContactDetail>
	<ID>0</ID>
	<name>gerry</name>
	<phonelist>
		<phone>186662936</phone>
		<phone>07552677</phone>
	</phonelist>
	<EmailList>
		<email>186662936</email>
		<email>07552677</email>
	</EmailList>
	<AddrList>
		<addr>186662936</addr>
		<addr>07552677</addr>
	</AddrList>
</ContactDetail> 
	 */
	
	String GetContactDetail(String sID){
		ContactDetailInfo c = ContactApi.GetContactsDetail(Long.parseLong(sID));
		StringBuilder sXml = new StringBuilder();
		sXml.append("<ContactDetail>");
		
		sXml.append("<ID>");
		sXml.append(c.lID);
		sXml.append("</ID>");
		
		sXml.append("<name>");
		sXml.append(c.sFirstName + " " + c.sLastName);
		sXml.append("</name>");
		
		if (c.phoneList != null && c.phoneList.size() > 0){
			//sXml.append("<phonelist>");
			for (int i = 0; i < c.phoneList.size(); ++i){
				sXml.append("<phone>");
				sXml.append(c.phoneList.get(i));
				sXml.append("</phone>");
			}
			//sXml.append("</phonelist>");
		}
		
		if (c.EMailList != null && c.EMailList.size() > 0){
			//sXml.append("<EmailList>");
			for (int i = 0; i < c.EMailList.size(); ++i){
				sXml.append("<email>");
				sXml.append(c.EMailList.get(i));
				sXml.append("</email>");
			}
			//sXml.append("</EmailList>");
		}
		
		if (c.AddrList != null && c.AddrList.size() > 0){
			//sXml.append("<AddrList>");
			for (int i = 0; i < c.AddrList.size(); ++i){
				c.AddrList.get(i);
				sXml.append("<addr>");
				sXml.append(c.AddrList.get(i));
				sXml.append("</addr>");
			}
			//sXml.append("</AddrList>");
		}
		
		sXml.append("</ContactDetail>");
		
		return sXml.toString();
	}

	//暂时不支持区分常用联系人及所有联系人
	public String ProcessRequest(String request, Properties parms) {
		try {
			int iFrom = 0;
			int iTo = 0;

			String sAction = parms.getProperty(EADefine.EA_ACT_ACTION_TAG,
					EADefine.EA_ACT_GET_CONTACT_LIST);
			if (sAction.equals(EADefine.EA_ACT_GET_CONTACT_LIST)) {
				String sFrom = parms.getProperty(EADefine.EA_ACT_FROM_TAG, "0");
				String sTo = parms.getProperty(EADefine.EA_ACT_TO_TAG, "0");

				iFrom = Integer.valueOf(sFrom);
				iTo = Integer.valueOf(sTo);

				int iTotalCount = ContactApi.GetContactCount();
				if (iFrom > iTotalCount || iTotalCount < 1) {
					return GenRetCode(EADefine.EA_RET_END_OF_FILE);
				}

				iTo = iTotalCount;

				// 这里需要分页处理
				return GetContactList(iFrom, iTo);
			}
			
			if (sAction.equals(EADefine.EA_ACT_GET_CONTACT_LIST_XML)) {
				String sFrom = parms.getProperty(EADefine.EA_ACT_FROM_TAG, "0");
				String sTo = parms.getProperty(EADefine.EA_ACT_TO_TAG, "0");

				iFrom = Integer.valueOf(sFrom);
				iTo = Integer.valueOf(sTo);

				int iTotalCount = ContactApi.GetContactCount();
				if (iFrom > iTotalCount || iTotalCount < 1) {
					return GenRetCode(EADefine.EA_RET_END_OF_FILE);
				}

				iTo = iTotalCount;

				// 这里需要分页处理
				return GetContactListInXml(iFrom, iTo);
			}
			
			if (sAction.equals(EADefine.EA_ACT_GET_CONTACT_DETAIL)) {
				String sID = parms.getProperty(EADefine.EA_ACT_ID_TAG, "0");
				return GetContactDetail(sID);
			}
						
			if (sAction.contains(EADefine.EA_ACT_INSERT_CONTACT) || sAction.contains(EADefine.EA_ACT_UPDATE_CONTACT)){
				String sID = parms.getProperty(EADefine.EA_ACT_ID_TAG, "0");
				String sCName = parms.getProperty(EADefine.EA_ACT_CONTACT_NAME_TAG, "");
				String sPhone = parms.getProperty(EADefine.EA_ACT_CONTACT_PHONE_TAG, "");
				String sAddr = parms.getProperty(EADefine.EA_ACT_CONTACT_ADDR_TAG, "");
				String sOrg = parms.getProperty(EADefine.EA_ACT_CONTACT_ORG_TAG, "");
				String sCMail = parms.getProperty(EADefine.EA_ACT_CONTACT_MAIL_TAG, "");
				String sNotes = parms.getProperty(EADefine.EA_ACT_CONTACT_NOTES_TAG, "");
				String sIM = parms.getProperty(EADefine.EA_ACT_CONTACT_IM_TAG, "");
				
				sID = EAUtil.CHECK_STRING(sID, "0");
				
				ContactDetailInfo c = ContactApi.ContactParser(sID,sCName, sPhone,sAddr, sOrg, sCMail, sNotes, sIM);
				boolean bRet = false;
				if (sAction.contains(EADefine.EA_ACT_INSERT_CONTACT)){
					bRet = ContactApi.insertContact(c);
				}else if (!sID.equals("0")){
					bRet = ContactApi.update(c);
				}else{
					return GenRetCode(EADefine.EA_RET_UNKONW_REQ);
				}
				
				if (bRet){
					ContactApi.InitCache();
					return GenRefreshCmd();
				}else{
					return GenRetCode(EADefine.EA_RET_FAILED);
				}
			}
			
			if (sAction.contains(EADefine.EA_ACT_DELETE_CONTACT)){
				String sCIDs = parms.getProperty(EADefine.EA_ACT_ID_TAG, "0");
				String[] sIDList = sCIDs.split(",");
				for (int i = 0; i < sIDList.length; ++i){
					if (sIDList[i] == null || sIDList[i].length() < 1){
						continue;
					}
					
					if (!ContactApi.delete(Long.parseLong(sIDList[i]))){
						ContactApi.InitCache();
						return GenRetCode(EADefine.EA_RET_FAILED);
					}
				}
				
				ContactApi.InitCache();
				return GenRefreshCmd();
			}
			
			return GenRetCode(EADefine.EA_RET_UNKONW_REQ);
		} catch (Exception e) {
			return ReturnException(e.toString());
		}

	}

	String GetContactList(int iFrom, int iTo) {
		/*
		 * <ContactList> <TotalCount>16</TotalCount>
		 * 
		 * <Contact> <ID></ID> <Name>Free mem</Name> <Number>0</Number>
		 * </Contact>
		 * 
		 * </ContactList>
		 */

		// String sContactEntryFmt =
		// "<Contact><ID>%d</ID><Name>%s</Name><Number>%s</Number></Contact>\r\n";

		int iCallCount = ContactApi.GetContactCount();
		if (iCallCount < iFrom) {
			return GenRetCode(EADefine.EA_RET_END_OF_FILE);
		}

		StringBuilder sXml = new StringBuilder();
/*		sXml.append("<ContactList>");

		sXml.append("<TotalCount>");
		sXml.append(iCallCount);
		sXml.append("</TotalCount>");*/

		if (iCallCount < iTo) {
			iTo = iCallCount;
		}

		if (iFrom < 0) {
			iFrom = 0;
		}

		List<ContactInfo> contactList = ContactApi.GetContactList(iFrom, iTo);
		
		if (contactList == null || contactList.size() < 1) {
			return GenRetCode(EADefine.EA_RET_END_OF_FILE);
		}
		
		iCallCount = contactList.size();
		
/*		<p class="cInfo">
		<label>Topsun<br> +8613649999999</label>
		<input type="checkbox"	value="true"  />
	</p>*/
		
		SetRespMimeType(NanoHTTPD.MIME_HTML);

		sXml.append("ContactTotalCount:");
		sXml.append(iCallCount);
			
		sXml.append(";ContactCount:");
		sXml.append(contactList.size());
		
		sXml.append(";RespType:ContactList" + EADefine.EA_ACT_HTML_SEPERATOR_TAG);
		
		for (int i = 0; i < contactList.size(); ++i) {
			/*
			 <dl class="contact_item">
          <dt><a href="#" onclick="OnAddContact2SelList(0);"><img src="img/select-contact.png"/></a>Topsun +8613649999999</dt>
        </dl>
			 */
			String sContacts = contactList.get(i).sName + "<" + contactList.get(i).sPhone + ">";
			
			sXml.append("<dl class=\"contact_item\"><dt>");

			sXml.append("<a href=\"#\" onclick='OnAddContact2SelList(\"");
			sXml.append(sContacts);
			sXml.append("\");'>");
			sXml.append("<img src=\"img/select-contact.png\"/></a>");
			sXml.append("<label>");
			sXml.append(sContacts);
			sXml.append("</label>");
			sXml.append("</dt></dl>");
		
			//sXml.append("<div class=\"divLine\"></div>");
		}

		return sXml.toString();
	}


	String GetContactListInXml(int iFrom, int iTo) {
		/*
		 * <ContactList> <TotalCount>16</TotalCount>
		 * 
		 * <Contact> <ID></ID> <Name>Free mem</Name> <Number>0</Number>
		 * </Contact>
		 * 
		 * </ContactList>
		 */
		int iCallCount = ContactApi.GetContactCount();
		if (iCallCount < iFrom) {
			return GenRetCode(EADefine.EA_RET_END_OF_FILE);
		}

		StringBuilder sXml = new StringBuilder();
		sXml.append("<ContactList>");

		sXml.append("<TotalCount>");
		sXml.append(iCallCount);
		sXml.append("</TotalCount>");

		if (iCallCount < iTo) {
			iTo = iCallCount;
		}

		if (iFrom < 0) {
			iFrom = 0;
		}

		List<ContactInfo> contactList = ContactApi.GetContactList(iFrom, iTo);
		
		if (contactList == null || contactList.size() < 1) {
			return GenRetCode(EADefine.EA_RET_END_OF_FILE);
		}
		
		for (int i = 0; i < contactList.size(); ++i) {
			// String sCallInfo = new String();
			sXml.append("<Contact>");

			sXml.append("<ID>");
			sXml.append(contactList.get(i).lID);
			sXml.append("</ID>");

			sXml.append("<Name>");
			sXml.append(contactList.get(i).sName);
			sXml.append("</Name>");

			sXml.append("<Number>");
			sXml.append(contactList.get(i).sPhone);
			sXml.append("</Number>");
						
			sXml.append("</Contact>");
		}
		sXml.append("</ContactList>");

		return sXml.toString();
	}

}
