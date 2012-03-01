package org.eclipse.osee.ats.core.client.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.osee.ats.core.model.IAtsUser;
import org.eclipse.osee.framework.core.exception.OseeCoreException;
import org.eclipse.osee.framework.jdk.core.util.Strings;
import org.eclipse.osee.framework.logging.OseeLog;
import org.eclipse.osee.framework.skynet.core.User;
import org.eclipse.osee.framework.skynet.core.UserManager;

/**
 * @author Donald G. Dunne
 */
public class UsersByIds {

   public static Pattern userPattern = Pattern.compile("<(.*?)>");

   public static String getStorageString(Collection<IAtsUser> users) throws OseeCoreException {
      StringBuffer sb = new StringBuffer();
      for (IAtsUser u : users) {
         sb.append("<" + u.getUserId() + ">");
      }
      return sb.toString();
   }

   public static List<IAtsUser> getUsers(String sorageString) {
      List<IAtsUser> users = new ArrayList<IAtsUser>();
      Matcher m = userPattern.matcher(sorageString);
      while (m.find()) {
         String userId = m.group(1);
         if (!Strings.isValid(userId)) {
            throw new IllegalArgumentException("Blank userId specified.");
         }
         try {
            User u = UserManager.getUserByUserId(m.group(1));
            users.add(new AtsUser(u));
         } catch (Exception ex) {
            OseeLog.log(org.eclipse.osee.ats.core.client.internal.Activator.class, Level.SEVERE, ex);
         }
      }
      return users;
   }

}
