/*******************************************************************************
VisibilitySegment.java
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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;

/**
 * A Line segment with added visibility value Visibility may be VISIBLE or HIDDEN
 *
 */
class VisibilitySegment
         extends
            LineSegment {
   private static final long serialVersionUID = -1834372981852774886L;
   private final int               m_visibility;


   public VisibilitySegment(final Coordinate startPoint,
                            final Coordinate endPoint,
                            final int visibility) {
      p0 = startPoint;
      p1 = endPoint;
      m_visibility = visibility;
   }


   public int getVisibility() {
      return m_visibility;
   }
}
