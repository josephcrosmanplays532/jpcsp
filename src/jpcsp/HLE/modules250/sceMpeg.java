/*
This file is part of jpcsp.

Jpcsp is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Jpcsp is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Jpcsp.  If not, see <http://www.gnu.org/licenses/>.
 */
package jpcsp.HLE.modules250;

import jpcsp.HLE.HLEFunction;

public class sceMpeg extends jpcsp.HLE.modules150.sceMpeg {
    @HLEFunction(nid = 0x769BEBB6, version = 250)
    public int sceMpegRingbufferQueryPackNum(int memorySize) {
        if (log.isDebugEnabled()) {
        	log.debug(String.format("sceMpegRingbufferQueryPackNum memorySize=0x%08X", memorySize));
        }

        return getPacketsFromSize(memorySize);
    }
}