/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
package test.util.ui;

import java.awt.Font;
import java.io.IOException;
import java.io.StringReader;

import util.ui.TextLineBreaker;
import junit.framework.TestCase;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class TextLineBreakerTest extends TestCase {

  public void testLineBreak() throws IOException {
    Font font = new Font("Dialog", Font.PLAIN, 12);
    
    TextLineBreaker breaker = new TextLineBreaker();

    // A normal line break    
    String text = "Es ging der Stiefel und sein Knecht von Kniggebühl nach " +
      "Entenbrecht.";
    StringReader reader = new StringReader(text);
    String[] lineArr = breaker.breakLines(reader, font, 100, 3);
    assertEquals(lineArr.length, 3);
    assertEquals(lineArr[0], "Es ging der Stiefel");
    assertEquals(lineArr[1], "und sein Knecht");
    assertEquals(lineArr[2], "von Kniggebühl...");

    // A long word break with good breaking chars
    text = "Da ist die Teta-Graphen-Hypernations-Maschine";
    reader = new StringReader(text);
    lineArr = breaker.breakLines(reader, font, 100, 4);
    assertEquals(lineArr.length, 4);
    assertEquals(lineArr[0], "Da ist die");
    assertEquals(lineArr[1], "Teta-Graphen-");
    assertEquals(lineArr[2], "Hypernations-");
    assertEquals(lineArr[3], "Maschine");
    
    // A long word break without good breaking chars
    text = "Parabailarlabambaparabailarlabambasenecesitaunacopadicracia";
    reader = new StringReader(text);
    lineArr = breaker.breakLines(reader, font, 100, 4);
    assertEquals(lineArr.length, 4);
    assertEquals(lineArr[0], "Parabailarlabam-");
    assertEquals(lineArr[1], "baparabailarlaba-");
    assertEquals(lineArr[2], "mbasenecesitau-");
    assertEquals(lineArr[3], "nacopadicracia");
    
    /*
    for (int i = 0; i < lineArr.length; i++) {
      System.out.println(lineArr[i]);
    }
    */
  }

}
