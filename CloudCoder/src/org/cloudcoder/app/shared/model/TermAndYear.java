// NetCoder - a web-based pedagogical programming environment
// Copyright (C) 2011, Jaime Spacco
// Copyright (C) 2011, David H. Hovemeyer
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

public class TermAndYear implements Comparable<TermAndYear> {
	private Term term;
	private int year;
	
	public TermAndYear() {
		
	}
	
	public TermAndYear(Term term, int year) {
		this.term = term;
		this.year = year;
	}
	
	public void setTerm(Term term) {
		this.term = term;
	}
	
	public Term getTerm() {
		return term;
	}
	
	public void setYear(int year) {
		this.year = year;
	}
	
	public int getYear() {
		return year;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		TermAndYear other = (TermAndYear) obj;
		return term.equals(other.term) && year == other.year;
	}
	
	@Override
	public int hashCode() {
		return term.hashCode() * 39 + year;
	}
	
	@Override
	public String toString() {
		return term.toString() + " " + year;
	}
	
	@Override
	public int compareTo(TermAndYear o) {
		// order descending (most recent term/year first)
		int cmp = this.year - o.year;
		if (cmp != 0) {
			return cmp * -1;
		}
		return (this.term.getSeq() - o.term.getSeq()) * -1;
	}
}