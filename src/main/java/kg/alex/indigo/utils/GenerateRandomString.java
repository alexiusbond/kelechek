/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.indigo.utils;

/**
 *
 * @author alex
 */

public class GenerateRandomString {
   private static final String ALPHA_NUM =
           "abcdefghjktuvwxyz123456789";
   
   public String getAlphaNumeric(int len) {
      StringBuilder sb = new StringBuilder(len);
      for (int i=0;  i<len;  i++) {
         int ndx = (int)(Math.random()*ALPHA_NUM.length());
         sb.append(ALPHA_NUM.charAt(ndx));
      }
      return sb.toString();
   }
}
