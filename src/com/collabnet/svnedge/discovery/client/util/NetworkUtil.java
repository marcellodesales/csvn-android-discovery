package com.collabnet.svnedge.discovery.client.util;

import java.net.Inet4Address;
import java.net.UnknownHostException;

/**
 * The utility class to work with lower-level network protocol.
 * 
 * @author Marcello de Sales (marcello.desales@gmail.com)
 * 
 */
public enum NetworkUtil {

    SINGLETON;

    /**
     * @param ipAddress is the textual representation of an ip address, which must be a string with 4 numbers delimited
     * by "."
     * @return the long representation of an ipAddress
     */
    public Long makeIntFromIpAddress(String ipAddress) {
        String[] addrArray = ipAddress.split("\\.");

        long num = 0;
        for (int i = 0; i < addrArray.length; i++) {
            int power = 3 - i;

            num += ((Integer.parseInt(addrArray[i]) % 256 * Math.pow(256, power)));
        }
        return num;
    }

    /**
     * The value of the ip
     * 
     * @param inetAddress is the instance of an ipAddress
     * @return the long representation of the textual ip representation of the given inetAddress
     */
    public Long makeIntFromIpAddress(Inet4Address inetAddress) {
        return makeIntFromIpAddress(inetAddress.getHostAddress());
    }

    /**
     * @param compressedIp the compressed IP address in an integer.
     * @return the octate with the values of the IP address like in [127,0,0,1] = localhost... in bytes.
     */
    private byte[] getIpBytesFromInt(Long compressedIp) {
        return new byte[] { (byte) (compressedIp & 0xff), (byte) (compressedIp >> 8 & 0xff),
                (byte) (compressedIp >> 16 & 0xff), (byte) (compressedIp >> 24 & 0xff) };
    }

    /**
     * Converts the given compressed IP address and transforms into the String representation. 1243452 -> 127.
     * 
     * @param compressedIp is the compressed IP. @see makeIntFromIpAddress(String ip).
     * @return the string representation of the compressed IP.
     */
    public String makeIpAddressFromInt(Long compressedIp) {
        StringBuilder builder = new StringBuilder();
        for (byte octal : this.getIpBytesFromInt(compressedIp)) {
            builder.append(octal);
            builder.append(".");
        }
        return builder.toString().substring(0, builder.toString().length() - 1);
    }

    /**
     * @param compressedIp
     * @return the IPv4 instance of the given IP address.
     */
    public Inet4Address makeInetAddressFromInt(Long compressedIp) {
        byte[] byteaddr = getIpBytesFromInt(compressedIp);
        try {
            return (Inet4Address) Inet4Address.getByAddress(byteaddr);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("The long value provided '" + compressedIp
                    + "' might not be from a valid ip Address");
        }
    }

    public static void main(String[] args) {
        String ipAddress = "192.168.48.13";
        System.out.println(ipAddress);
        Long compressedIp = NetworkUtil.SINGLETON.makeIntFromIpAddress(ipAddress);
        System.out.println(compressedIp);
        String retrievedIp = NetworkUtil.SINGLETON.makeIpAddressFromInt(compressedIp);
        Inet4Address inetAddress = NetworkUtil.SINGLETON.makeInetAddressFromInt(compressedIp);
        System.out.println(retrievedIp);
        System.out.println(retrievedIp.equals(ipAddress));
        System.out.println(inetAddress);
        System.out.println(inetAddress.getHostAddress().equals(ipAddress));

    }
}
