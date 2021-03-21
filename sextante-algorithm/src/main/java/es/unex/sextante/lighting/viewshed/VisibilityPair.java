/*******************************************************************************
VisibilityPair.java
Copyright (C) Aviad Segev, 2010

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *******************************************************************************/

package es.unex.sextante.lighting.viewshed;

/**
 * A pair of visibility segments
 *
 */
class VisibilityPair {
   private final VisibilitySegment m_firstSegment;
   private final VisibilitySegment m_secondSegment;


   public VisibilityPair(final VisibilitySegment firstROSSegment,
                         final VisibilitySegment secondROSSegment) {
      m_firstSegment = firstROSSegment;
      m_secondSegment = secondROSSegment;
   }


   public VisibilitySegment getFirstSegment() {
      return m_firstSegment;
   }


   public VisibilitySegment getSecondSegment() {
      return m_secondSegment;
   }
}
