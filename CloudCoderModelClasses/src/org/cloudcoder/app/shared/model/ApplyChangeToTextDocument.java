// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.cloudcoder.app.shared.model;


/**
 * Apply a Change object to a TextDocument.
 */
public class ApplyChangeToTextDocument {
	private static final boolean DEBUG = true;

	/**
	 * Apply a Change object to a TextDocument.
	 * 
	 * @param change a Change object
	 * @param doc    a TextDocument to which the Change should be applied
	 */
	public void apply(Change change, TextDocument doc) {
		String s, up;
		
		switch (change.getType()) {
		case INSERT_TEXT:
			if (change.getStartRow() == doc.getNumLines()) {
				doc.append("");
			}
			s = doc.getLine(change.getStartRow());
			up = s.substring(0, change.getStartColumn()) + change.getText() + s.substring(change.getStartColumn());
			changeLine(doc, change.getStartRow(), up);
			break;
		case REMOVE_TEXT:
		    /*
		     * Remove text can be:
		     * 
		     * null removes (WTF are these?):  start_row==end_row, empty text_short
		     * short removes: start_row==end_row, non-empty text_short
		     * multi-line short removes: start_row<end_row, non-empty text_short
		     * multi-line long removes: start_row==end_row, non-empty text 
		     * 
		     * While the DB stores both text_short (varchar) and text (smalltext), the
		     * Java code combines both into one String field.
		     * 
		     * Thus multi-line short remove and multi-line long removes are essentially the same.
		     * 
		     */
		    
		    if (change.getStartRow() > change.getEndRow()) {
		        // This should never happen
                throw new IllegalStateException(change+" is not legal because startRow > endRow");
            }
		    
		    if (change.getStartRow()==change.getEndRow()) {
		        if (change.getText().equals("")) {
		            // null remove; does this make any sense?
		            // what the heck would we be removing?
		        } else {
		            // one-line short remove (no newlines)
                    String line = doc.getLine(change.getStartRow());
                    String newline = line.substring(0, change.getStartColumn()) + 
                            line.substring(change.getStartColumn()+change.getText().length());
                    doc.setLine(change.getStartRow(), newline);
		        }
		    } else {
		        // change.getStartRow() < change.getEndRow()
		        if (change.getText().equals("\n") || change.getText().equals("\r\n")) {
		            if (change.getStartRow() + 1 != change.getEndRow()) {
		                throw new IllegalArgumentException("unexpected REMOVE_TEXT to remove newline: " + change);
		            }
		            // combine with line below
		            s = doc.getLine(change.getStartRow());
		            up = s.substring(0, s.length() - 1) + doc.getLine(change.getEndRow());
		            doc.setLine(change.getStartRow(), up);
		            doc.removeLine(change.getEndRow());
		        } else {
		            // multi-line remove
		            // all changes seen so far have newlines in the removed text
		            // XXX Is it possible for a multi-line remove
		            String changeText=change.getText();
		            // XXX try to remove \r to standardize on \n unix line endings
		            changeText=changeText.replaceAll("\r", "");

		            String[] textChanges=changeText.split("\n");

		            // adjust first line by removing everything after startCol
		            String firstLine=doc.getLine(change.getStartRow());
		            doc.setLine(change.getStartRow(), firstLine.substring(0, change.getStartColumn()));

		            // delete lines startRow+1 through endRow-1
		            for (int i=change.getStartRow(); i<change.getEndRow(); i++) {
		                // have to keep removing startRow+1 since each removal shifts the
		                // remaining lines into that position
		                doc.removeLine(change.getStartRow()+1);
		            }
		            // the final line is now located at startRow+1
		            String lastLine=doc.getLine(change.getStartRow()+1);
		            String lastChangeLine=textChanges[textChanges.length-1];

		            lastLine=lastLine.substring(lastChangeLine.length());
		            doc.setLine(change.getStartRow()+1, lastLine);
		        }
		    }

//			} else if (change.getStartRow()==change.getEndRow() &&
//			        change.getStartColumn()==change.getEndColumn() &&
//			        !change.getText().equals(""))
//			{
//				s = doc.getLine(change.getStartRow());
//				// REMOVE_TEXT seems to store the text to be removed
//				// and the location where it should be removed from
//				up = s.substring(0, change.getStartColumn()) + 
//				        s.substring(change.getStartColumn()+change.getText().length());
//				doc.setLine(change.getStartRow(), up);
//			} else {
//			    // TODO handle other case
//			    break;
//			}
			
			break;
		case INSERT_LINES:
			for (int i = 0; i < change.getNumLines(); i++) {
				doc.insertLine(change.getStartRow() + i, change.getLine(i) + "\n");
			}
			break;
		case REMOVE_LINES:
			for (int i = 0; i < change.getNumLines(); i++) {
				doc.removeLine(change.getStartRow());
			}
			break;
		case FULL_TEXT:
			doc.setText(change.getText());
			break;
		default:
			throw new IllegalStateException("Not handled? " + change.getType());
		}
		
		// check integrity of TextDocument
		if (DEBUG) {
			for (int i = 0; i < doc.getNumLines(); i++) {
				String line = doc.getLine(i);
				int nl = line.indexOf('\n');
				if (nl >= 0 && nl != line.length() - 1) {
					throw new IllegalStateException("Line has enbedded newline!");
				}
			}
		}
	}
	
	/**
	 * Change text at given line, inserting multiple lines as necessary
	 * if text has embedded newlines.
	 * 
	 * @param doc   the TextDocument
	 * @param index index of line to change
	 * @param text  text to put at given index
	 */
	private void changeLine(TextDocument doc, int index, String text) {
		int nl = text.indexOf('\n');

		if (nl < 0 || nl == text.length() - 1) {
			// Line either has no newline, or there is only one newline at
			// the end of the line
			doc.setLine(index, text);
			return;
		}
		
		// line contains embedded newlines: need to split
		doc.removeLine(index);
		boolean done = false;
		while (!done) {
			doc.insertLine(index, text.substring(0, nl+1));
			index++;
			text = text.substring(nl + 1);
			nl = text.indexOf('\n');
			done = (nl < 0);
		}
	}
}
