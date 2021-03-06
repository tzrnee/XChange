package com.xeiam.xchange.streaming.websocket.drafts;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

import com.xeiam.xchange.streaming.websocket.DefaultHandshakeData;
import com.xeiam.xchange.streaming.websocket.HandshakeBuilder;
import com.xeiam.xchange.streaming.websocket.HandshakeData;
import com.xeiam.xchange.streaming.websocket.WebSocket.Role;
import com.xeiam.xchange.streaming.websocket.exceptions.IncompleteHandshakeException;
import com.xeiam.xchange.streaming.websocket.exceptions.InvalidHandshakeException;

public class Draft_76 extends Draft_75 {

  public static byte[] createChallenge(String key1, String key2, byte[] key3) throws InvalidHandshakeException {

    byte[] part1 = getPart(key1);
    byte[] part2 = getPart(key2);
    byte[] challenge = new byte[16];
    challenge[0] = part1[0];
    challenge[1] = part1[1];
    challenge[2] = part1[2];
    challenge[3] = part1[3];
    challenge[4] = part2[0];
    challenge[5] = part2[1];
    challenge[6] = part2[2];
    challenge[7] = part2[3];
    challenge[8] = key3[0];
    challenge[9] = key3[1];
    challenge[10] = key3[2];
    challenge[11] = key3[3];
    challenge[12] = key3[4];
    challenge[13] = key3[5];
    challenge[14] = key3[6];
    challenge[15] = key3[7];
    MessageDigest md5;
    try {
      md5 = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
    return md5.digest(challenge);
  }

  private static String generateKey() {

    Random r = new Random();
    long maxNumber = 4294967295L;
    long spaces = r.nextInt(12) + 1;
    int max = new Long(maxNumber / spaces).intValue();
    max = Math.abs(max);
    int number = r.nextInt(max) + 1;
    long product = number * spaces;
    String key = Long.toString(product);
    // always insert atleast one random character
    int numChars = r.nextInt(12) + 1;
    for (int i = 0; i < numChars; i++) {
      int position = r.nextInt(key.length());
      position = Math.abs(position);
      char randChar = (char) (r.nextInt(95) + 33);
      // exclude numbers here
      if (randChar >= 48 && randChar <= 57) {
        randChar -= 15;
      }
      key = new StringBuilder(key).insert(position, randChar).toString();
    }
    for (int i = 0; i < spaces; i++) {
      int position = r.nextInt(key.length() - 1) + 1;
      position = Math.abs(position);
      key = new StringBuilder(key).insert(position, "\u0020").toString();
    }
    return key;
  }

  private static byte[] getPart(String key) throws InvalidHandshakeException {

    try {
      long keyNumber = Long.parseLong(key.replaceAll("[^0-9]", ""));
      long keySpace = key.split("\u0020").length - 1;
      if (keySpace == 0) {
        throw new InvalidHandshakeException("invalid Sec-WebSocket-Key (/key2/)");
      }
      long part = new Long(keyNumber / keySpace);
      return new byte[] { (byte) (part >> 24), (byte) ((part << 8) >> 24), (byte) ((part << 16) >> 24), (byte) ((part << 24) >> 24) };
    } catch (NumberFormatException e) {
      throw new InvalidHandshakeException("invalid Sec-WebSocket-Key (/key1/ or /key2/)");
    }
  }

  private boolean failed = false;

  @Override
  public HandshakeState acceptHandshakeAsClient(HandshakeData request, HandshakeData response) {

    if (failed) {
      return HandshakeState.NOT_MATCHED;
    }

    try {
      if (!response.getFieldValue("Sec-WebSocket-Origin").equals(request.getFieldValue("Origin")) || !basicAccept(response)) {
        return HandshakeState.NOT_MATCHED;
      }
      byte[] content = response.getContent();
      if (content == null || content.length == 0) {
        HandshakeBuilder builder = new DefaultHandshakeData(response);
        return HandshakeState.MATCHING;
      }
      if (Arrays.equals(content, createChallenge(request.getFieldValue("Sec-WebSocket-Key1"), request.getFieldValue("Sec-WebSocket-Key2"), request.getContent()))) {
        return HandshakeState.MATCHED;
      } else {
        return HandshakeState.NOT_MATCHED;
      }
    } catch (InvalidHandshakeException e) {
      throw new RuntimeException("bad handshakerequest", e);
    }
  }

  @Override
  public HandshakeState acceptHandshakeAsServer(HandshakeData handshakeData) {

    if (handshakeData.getFieldValue("Upgrade").equals("WebSocket") && handshakeData.getFieldValue("Connection").contains("Upgrade") && handshakeData.getFieldValue("Sec-WebSocket-Key1").length() > 0
        && !handshakeData.getFieldValue("Sec-WebSocket-Key2").isEmpty() && handshakeData.hasFieldValue("Origin")
    /* new String ( handshakeData.getContent () ).endsWith ( "\r\n\r\n" ) */)
      return HandshakeState.MATCHED;
    return HandshakeState.NOT_MATCHED;
  }

  @Override
  public HandshakeBuilder postProcessHandshakeRequestAsClient(HandshakeBuilder request) {

    request.put("Upgrade", "WebSocket");
    request.put("Connection", "Upgrade");
    request.put("Sec-WebSocket-Key1", generateKey());
    request.put("Sec-WebSocket-Key2", generateKey());
    byte[] key3 = new byte[8];
    (new Random()).nextBytes(key3);
    request.setContent(key3);
    return request;

  }

  @Override
  public HandshakeBuilder postProcessHandshakeResponseAsServer(HandshakeData request, HandshakeBuilder response) throws InvalidHandshakeException {

    response.setHttpStatusMessage("WebSocket Protocol Handshake");
    response.put("Upgrade", "WebSocket");
    response.put("Connection", request.getFieldValue("Connection")); // to respond to a Connection keep alive
    response.put("Sec-WebSocket-Origin", request.getFieldValue("Origin"));
    String location = "ws://" + request.getFieldValue("Host") + request.getResourceDescriptor();
    response.put("Sec-WebSocket-Location", location);
    String key1 = request.getFieldValue("Sec-WebSocket-Key1");
    String key2 = request.getFieldValue("Sec-WebSocket-Key2");
    byte[] key3 = request.getContent();
    if (key1 == null || key2 == null || key3 == null || key3.length != 8) {
      throw new InvalidHandshakeException("Bad keys");
    }
    response.setContent(createChallenge(key1, key2, key3));
    return response;
  }

  @Override
  public HandshakeData translateHandshake(ByteBuffer buf) throws InvalidHandshakeException {

    HandshakeBuilder bui = translateHandshakeHttp(buf);
    byte[] key3 = new byte[role == Role.SERVER ? 8 : 16];
    try {
      buf.get(key3);
    } catch (BufferUnderflowException e) {
      throw new IncompleteHandshakeException();
    }
    bui.setContent(key3);
    return bui;
  }
}
