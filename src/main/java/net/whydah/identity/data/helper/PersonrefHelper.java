package net.whydah.identity.data.helper;

/**
 * Personrefdeobfuskator.
 */
public class PersonrefHelper {
    /**
     * Deobfuskerer personref og gir personnummer.
     * @param personref
     * @return personnummer.
     */
    public static String decodePersonref(String personref) {
        byte[] inbytes = personref.getBytes();
        byte[] outbytes = new byte[11];
        int shift = (inbytes[1] - 48) * 2 + inbytes[13] - 48;
        for (int i=2; i<13; i++) {
          outbytes[i-2] = (byte)(((inbytes[i] + (10-shift) - 48) % 10) + 48);
        }
        return new String(outbytes);
    }
}
