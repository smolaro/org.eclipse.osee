/*
 * Created on Apr 20, 2009
 *
 * PLACE_YOUR_DISTRIBUTION_STATEMENT_RIGHT_HERE
 */
package org.eclipse.osee.framework.messaging;

/**
 * @author b1528444
 *
 */
public interface ReceiveListener {
   void handle(Message cmd);
}
