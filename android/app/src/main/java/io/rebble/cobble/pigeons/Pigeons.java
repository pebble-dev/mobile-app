// Autogenerated from Pigeon (v0.1.12), do not edit directly.
// See also: https://pub.dev/packages/pigeon

package io.rebble.cobble.pigeons;

import io.flutter.plugin.common.BasicMessageChannel;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.StandardMessageCodec;
import java.util.ArrayList;
import java.util.HashMap;

/** Generated class from Pigeon. */
@SuppressWarnings("unused")
public class Pigeons {

  /** Generated class from Pigeon that represents data sent in messages. */
  public static class BooleanWrapper {
    private Boolean value;
    public Boolean getValue() { return value; }
    public void setValue(Boolean setterArg) { this.value = setterArg; }

    HashMap toMap() {
      HashMap<String, Object> toMapResult = new HashMap<>();
      toMapResult.put("value", value);
      return toMapResult;
    }
    static BooleanWrapper fromMap(HashMap map) {
      BooleanWrapper fromMapResult = new BooleanWrapper();
      Object value = map.get("value");
      fromMapResult.value = (Boolean)value;
      return fromMapResult;
    }
  }

  /** Generated class from Pigeon that represents data sent in messages. */
  public static class NumberWrapper {
    private Long value;
    public Long getValue() { return value; }
    public void setValue(Long setterArg) { this.value = setterArg; }

    HashMap toMap() {
      HashMap<String, Object> toMapResult = new HashMap<>();
      toMapResult.put("value", value);
      return toMapResult;
    }
    static NumberWrapper fromMap(HashMap map) {
      NumberWrapper fromMapResult = new NumberWrapper();
      Object value = map.get("value");
      fromMapResult.value = (value == null) ? null : ((value instanceof Integer) ? (Integer)value : (Long)value);
      return fromMapResult;
    }
  }

  /** Generated class from Pigeon that represents data sent in messages. */
  public static class TimelinePinPigeon {
    private String itemId;
    public String getItemId() { return itemId; }
    public void setItemId(String setterArg) { this.itemId = setterArg; }

    private String parentId;
    public String getParentId() { return parentId; }
    public void setParentId(String setterArg) { this.parentId = setterArg; }

    private Long timestamp;
    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long setterArg) { this.timestamp = setterArg; }

    private Long type;
    public Long getType() { return type; }
    public void setType(Long setterArg) { this.type = setterArg; }

    private Long duration;
    public Long getDuration() { return duration; }
    public void setDuration(Long setterArg) { this.duration = setterArg; }

    private Boolean isVisible;
    public Boolean getIsVisible() { return isVisible; }
    public void setIsVisible(Boolean setterArg) { this.isVisible = setterArg; }

    private Boolean isFloating;
    public Boolean getIsFloating() { return isFloating; }
    public void setIsFloating(Boolean setterArg) { this.isFloating = setterArg; }

    private Boolean isAllDay;
    public Boolean getIsAllDay() { return isAllDay; }
    public void setIsAllDay(Boolean setterArg) { this.isAllDay = setterArg; }

    private Boolean persistQuickView;
    public Boolean getPersistQuickView() { return persistQuickView; }
    public void setPersistQuickView(Boolean setterArg) { this.persistQuickView = setterArg; }

    private Long layout;
    public Long getLayout() { return layout; }
    public void setLayout(Long setterArg) { this.layout = setterArg; }

    private String attributesJson;
    public String getAttributesJson() { return attributesJson; }
    public void setAttributesJson(String setterArg) { this.attributesJson = setterArg; }

    private String actionsJson;
    public String getActionsJson() { return actionsJson; }
    public void setActionsJson(String setterArg) { this.actionsJson = setterArg; }

    HashMap toMap() {
      HashMap<String, Object> toMapResult = new HashMap<>();
      toMapResult.put("itemId", itemId);
      toMapResult.put("parentId", parentId);
      toMapResult.put("timestamp", timestamp);
      toMapResult.put("type", type);
      toMapResult.put("duration", duration);
      toMapResult.put("isVisible", isVisible);
      toMapResult.put("isFloating", isFloating);
      toMapResult.put("isAllDay", isAllDay);
      toMapResult.put("persistQuickView", persistQuickView);
      toMapResult.put("layout", layout);
      toMapResult.put("attributesJson", attributesJson);
      toMapResult.put("actionsJson", actionsJson);
      return toMapResult;
    }
    static TimelinePinPigeon fromMap(HashMap map) {
      TimelinePinPigeon fromMapResult = new TimelinePinPigeon();
      Object itemId = map.get("itemId");
      fromMapResult.itemId = (String)itemId;
      Object parentId = map.get("parentId");
      fromMapResult.parentId = (String)parentId;
      Object timestamp = map.get("timestamp");
      fromMapResult.timestamp = (timestamp == null) ? null : ((timestamp instanceof Integer) ? (Integer)timestamp : (Long)timestamp);
      Object type = map.get("type");
      fromMapResult.type = (type == null) ? null : ((type instanceof Integer) ? (Integer)type : (Long)type);
      Object duration = map.get("duration");
      fromMapResult.duration = (duration == null) ? null : ((duration instanceof Integer) ? (Integer)duration : (Long)duration);
      Object isVisible = map.get("isVisible");
      fromMapResult.isVisible = (Boolean)isVisible;
      Object isFloating = map.get("isFloating");
      fromMapResult.isFloating = (Boolean)isFloating;
      Object isAllDay = map.get("isAllDay");
      fromMapResult.isAllDay = (Boolean)isAllDay;
      Object persistQuickView = map.get("persistQuickView");
      fromMapResult.persistQuickView = (Boolean)persistQuickView;
      Object layout = map.get("layout");
      fromMapResult.layout = (layout == null) ? null : ((layout instanceof Integer) ? (Integer)layout : (Long)layout);
      Object attributesJson = map.get("attributesJson");
      fromMapResult.attributesJson = (String)attributesJson;
      Object actionsJson = map.get("actionsJson");
      fromMapResult.actionsJson = (String)actionsJson;
      return fromMapResult;
    }
  }

  /** Generated class from Pigeon that represents data sent in messages. */
  public static class StringWrapper {
    private String value;
    public String getValue() { return value; }
    public void setValue(String setterArg) { this.value = setterArg; }

    HashMap toMap() {
      HashMap<String, Object> toMapResult = new HashMap<>();
      toMapResult.put("value", value);
      return toMapResult;
    }
    static StringWrapper fromMap(HashMap map) {
      StringWrapper fromMapResult = new StringWrapper();
      Object value = map.get("value");
      fromMapResult.value = (String)value;
      return fromMapResult;
    }
  }

  /** Generated class from Pigeon that represents data sent in messages. */
  public static class PebbleScanDevicePigeon {
    private String name;
    public String getName() { return name; }
    public void setName(String setterArg) { this.name = setterArg; }

    private Long address;
    public Long getAddress() { return address; }
    public void setAddress(Long setterArg) { this.address = setterArg; }

    private String version;
    public String getVersion() { return version; }
    public void setVersion(String setterArg) { this.version = setterArg; }

    private String serialNumber;
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String setterArg) { this.serialNumber = setterArg; }

    private Long color;
    public Long getColor() { return color; }
    public void setColor(Long setterArg) { this.color = setterArg; }

    private Boolean runningPRF;
    public Boolean getRunningPRF() { return runningPRF; }
    public void setRunningPRF(Boolean setterArg) { this.runningPRF = setterArg; }

    private Boolean firstUse;
    public Boolean getFirstUse() { return firstUse; }
    public void setFirstUse(Boolean setterArg) { this.firstUse = setterArg; }

    HashMap toMap() {
      HashMap<String, Object> toMapResult = new HashMap<>();
      toMapResult.put("name", name);
      toMapResult.put("address", address);
      toMapResult.put("version", version);
      toMapResult.put("serialNumber", serialNumber);
      toMapResult.put("color", color);
      toMapResult.put("runningPRF", runningPRF);
      toMapResult.put("firstUse", firstUse);
      return toMapResult;
    }
    static PebbleScanDevicePigeon fromMap(HashMap map) {
      PebbleScanDevicePigeon fromMapResult = new PebbleScanDevicePigeon();
      Object name = map.get("name");
      fromMapResult.name = (String)name;
      Object address = map.get("address");
      fromMapResult.address = (address == null) ? null : ((address instanceof Integer) ? (Integer)address : (Long)address);
      Object version = map.get("version");
      fromMapResult.version = (String)version;
      Object serialNumber = map.get("serialNumber");
      fromMapResult.serialNumber = (String)serialNumber;
      Object color = map.get("color");
      fromMapResult.color = (color == null) ? null : ((color instanceof Integer) ? (Integer)color : (Long)color);
      Object runningPRF = map.get("runningPRF");
      fromMapResult.runningPRF = (Boolean)runningPRF;
      Object firstUse = map.get("firstUse");
      fromMapResult.firstUse = (Boolean)firstUse;
      return fromMapResult;
    }
  }

  /** Generated class from Pigeon that represents data sent in messages. */
  public static class ListWrapper {
    private ArrayList value;
    public ArrayList getValue() { return value; }
    public void setValue(ArrayList setterArg) { this.value = setterArg; }

    HashMap toMap() {
      HashMap<String, Object> toMapResult = new HashMap<>();
      toMapResult.put("value", value);
      return toMapResult;
    }
    static ListWrapper fromMap(HashMap map) {
      ListWrapper fromMapResult = new ListWrapper();
      Object value = map.get("value");
      fromMapResult.value = (ArrayList)value;
      return fromMapResult;
    }
  }

  /** Generated class from Pigeon that represents data sent in messages. */
  public static class WatchConnectionStatePigeon {
    private Boolean isConnected;
    public Boolean getIsConnected() { return isConnected; }
    public void setIsConnected(Boolean setterArg) { this.isConnected = setterArg; }

    private Boolean isConnecting;
    public Boolean getIsConnecting() { return isConnecting; }
    public void setIsConnecting(Boolean setterArg) { this.isConnecting = setterArg; }

    private Long currentWatchAddress;
    public Long getCurrentWatchAddress() { return currentWatchAddress; }
    public void setCurrentWatchAddress(Long setterArg) { this.currentWatchAddress = setterArg; }

    private PebbleDevicePigeon currentConnectedWatch;
    public PebbleDevicePigeon getCurrentConnectedWatch() { return currentConnectedWatch; }
    public void setCurrentConnectedWatch(PebbleDevicePigeon setterArg) { this.currentConnectedWatch = setterArg; }

    HashMap toMap() {
      HashMap<String, Object> toMapResult = new HashMap<>();
      toMapResult.put("isConnected", isConnected);
      toMapResult.put("isConnecting", isConnecting);
      toMapResult.put("currentWatchAddress", currentWatchAddress);
      toMapResult.put("currentConnectedWatch", currentConnectedWatch.toMap());
      return toMapResult;
    }
    static WatchConnectionStatePigeon fromMap(HashMap map) {
      WatchConnectionStatePigeon fromMapResult = new WatchConnectionStatePigeon();
      Object isConnected = map.get("isConnected");
      fromMapResult.isConnected = (Boolean)isConnected;
      Object isConnecting = map.get("isConnecting");
      fromMapResult.isConnecting = (Boolean)isConnecting;
      Object currentWatchAddress = map.get("currentWatchAddress");
      fromMapResult.currentWatchAddress = (currentWatchAddress == null) ? null : ((currentWatchAddress instanceof Integer) ? (Integer)currentWatchAddress : (Long)currentWatchAddress);
      Object currentConnectedWatch = map.get("currentConnectedWatch");
      fromMapResult.currentConnectedWatch = PebbleDevicePigeon.fromMap((HashMap)currentConnectedWatch);
      return fromMapResult;
    }
  }

  /** Generated class from Pigeon that represents data sent in messages. */
  public static class PebbleDevicePigeon {
    private String name;
    public String getName() { return name; }
    public void setName(String setterArg) { this.name = setterArg; }

    private Long address;
    public Long getAddress() { return address; }
    public void setAddress(Long setterArg) { this.address = setterArg; }

    private PebbleFirmwarePigeon runningFirmware;
    public PebbleFirmwarePigeon getRunningFirmware() { return runningFirmware; }
    public void setRunningFirmware(PebbleFirmwarePigeon setterArg) { this.runningFirmware = setterArg; }

    private PebbleFirmwarePigeon recoveryFirmware;
    public PebbleFirmwarePigeon getRecoveryFirmware() { return recoveryFirmware; }
    public void setRecoveryFirmware(PebbleFirmwarePigeon setterArg) { this.recoveryFirmware = setterArg; }

    private Long model;
    public Long getModel() { return model; }
    public void setModel(Long setterArg) { this.model = setterArg; }

    private Long bootloaderTimestamp;
    public Long getBootloaderTimestamp() { return bootloaderTimestamp; }
    public void setBootloaderTimestamp(Long setterArg) { this.bootloaderTimestamp = setterArg; }

    private String board;
    public String getBoard() { return board; }
    public void setBoard(String setterArg) { this.board = setterArg; }

    private String serial;
    public String getSerial() { return serial; }
    public void setSerial(String setterArg) { this.serial = setterArg; }

    private String language;
    public String getLanguage() { return language; }
    public void setLanguage(String setterArg) { this.language = setterArg; }

    private Long languageVersion;
    public Long getLanguageVersion() { return languageVersion; }
    public void setLanguageVersion(Long setterArg) { this.languageVersion = setterArg; }

    private Boolean isUnfaithful;
    public Boolean getIsUnfaithful() { return isUnfaithful; }
    public void setIsUnfaithful(Boolean setterArg) { this.isUnfaithful = setterArg; }

    HashMap toMap() {
      HashMap<String, Object> toMapResult = new HashMap<>();
      toMapResult.put("name", name);
      toMapResult.put("address", address);
      toMapResult.put("runningFirmware", runningFirmware.toMap());
      toMapResult.put("recoveryFirmware", recoveryFirmware.toMap());
      toMapResult.put("model", model);
      toMapResult.put("bootloaderTimestamp", bootloaderTimestamp);
      toMapResult.put("board", board);
      toMapResult.put("serial", serial);
      toMapResult.put("language", language);
      toMapResult.put("languageVersion", languageVersion);
      toMapResult.put("isUnfaithful", isUnfaithful);
      return toMapResult;
    }
    static PebbleDevicePigeon fromMap(HashMap map) {
      PebbleDevicePigeon fromMapResult = new PebbleDevicePigeon();
      Object name = map.get("name");
      fromMapResult.name = (String)name;
      Object address = map.get("address");
      fromMapResult.address = (address == null) ? null : ((address instanceof Integer) ? (Integer)address : (Long)address);
      Object runningFirmware = map.get("runningFirmware");
      fromMapResult.runningFirmware = PebbleFirmwarePigeon.fromMap((HashMap)runningFirmware);
      Object recoveryFirmware = map.get("recoveryFirmware");
      fromMapResult.recoveryFirmware = PebbleFirmwarePigeon.fromMap((HashMap)recoveryFirmware);
      Object model = map.get("model");
      fromMapResult.model = (model == null) ? null : ((model instanceof Integer) ? (Integer)model : (Long)model);
      Object bootloaderTimestamp = map.get("bootloaderTimestamp");
      fromMapResult.bootloaderTimestamp = (bootloaderTimestamp == null) ? null : ((bootloaderTimestamp instanceof Integer) ? (Integer)bootloaderTimestamp : (Long)bootloaderTimestamp);
      Object board = map.get("board");
      fromMapResult.board = (String)board;
      Object serial = map.get("serial");
      fromMapResult.serial = (String)serial;
      Object language = map.get("language");
      fromMapResult.language = (String)language;
      Object languageVersion = map.get("languageVersion");
      fromMapResult.languageVersion = (languageVersion == null) ? null : ((languageVersion instanceof Integer) ? (Integer)languageVersion : (Long)languageVersion);
      Object isUnfaithful = map.get("isUnfaithful");
      fromMapResult.isUnfaithful = (Boolean)isUnfaithful;
      return fromMapResult;
    }
  }

  /** Generated class from Pigeon that represents data sent in messages. */
  public static class PebbleFirmwarePigeon {
    private Long timestamp;
    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long setterArg) { this.timestamp = setterArg; }

    private String version;
    public String getVersion() { return version; }
    public void setVersion(String setterArg) { this.version = setterArg; }

    private String gitHash;
    public String getGitHash() { return gitHash; }
    public void setGitHash(String setterArg) { this.gitHash = setterArg; }

    private Boolean isRecovery;
    public Boolean getIsRecovery() { return isRecovery; }
    public void setIsRecovery(Boolean setterArg) { this.isRecovery = setterArg; }

    private Long hardwarePlatform;
    public Long getHardwarePlatform() { return hardwarePlatform; }
    public void setHardwarePlatform(Long setterArg) { this.hardwarePlatform = setterArg; }

    private Long metadataVersion;
    public Long getMetadataVersion() { return metadataVersion; }
    public void setMetadataVersion(Long setterArg) { this.metadataVersion = setterArg; }

    HashMap toMap() {
      HashMap<String, Object> toMapResult = new HashMap<>();
      toMapResult.put("timestamp", timestamp);
      toMapResult.put("version", version);
      toMapResult.put("gitHash", gitHash);
      toMapResult.put("isRecovery", isRecovery);
      toMapResult.put("hardwarePlatform", hardwarePlatform);
      toMapResult.put("metadataVersion", metadataVersion);
      return toMapResult;
    }
    static PebbleFirmwarePigeon fromMap(HashMap map) {
      PebbleFirmwarePigeon fromMapResult = new PebbleFirmwarePigeon();
      Object timestamp = map.get("timestamp");
      fromMapResult.timestamp = (timestamp == null) ? null : ((timestamp instanceof Integer) ? (Integer)timestamp : (Long)timestamp);
      Object version = map.get("version");
      fromMapResult.version = (String)version;
      Object gitHash = map.get("gitHash");
      fromMapResult.gitHash = (String)gitHash;
      Object isRecovery = map.get("isRecovery");
      fromMapResult.isRecovery = (Boolean)isRecovery;
      Object hardwarePlatform = map.get("hardwarePlatform");
      fromMapResult.hardwarePlatform = (hardwarePlatform == null) ? null : ((hardwarePlatform instanceof Integer) ? (Integer)hardwarePlatform : (Long)hardwarePlatform);
      Object metadataVersion = map.get("metadataVersion");
      fromMapResult.metadataVersion = (metadataVersion == null) ? null : ((metadataVersion instanceof Integer) ? (Integer)metadataVersion : (Long)metadataVersion);
      return fromMapResult;
    }
  }

  /** Generated interface from Pigeon that represents a handler of messages from Flutter.*/
  public interface ScanControl {
    void startBleScan();
    void startClassicScan();

    /** Sets up an instance of `ScanControl` to handle messages through the `binaryMessenger` */
    static void setup(BinaryMessenger binaryMessenger, ScanControl api) {
      {
        BasicMessageChannel<Object> channel =
            new BasicMessageChannel<>(binaryMessenger, "dev.flutter.pigeon.ScanControl.startBleScan", new StandardMessageCodec());
        if (api != null) {
          channel.setMessageHandler((message, reply) -> {
            HashMap<String, HashMap> wrapped = new HashMap<>();
            try {
              api.startBleScan();
              wrapped.put("result", null);
            }
            catch (Exception exception) {
              wrapped.put("error", wrapError(exception));
            }
            reply.reply(wrapped);
          });
        } else {
          channel.setMessageHandler(null);
        }
      }
      {
        BasicMessageChannel<Object> channel =
            new BasicMessageChannel<>(binaryMessenger, "dev.flutter.pigeon.ScanControl.startClassicScan", new StandardMessageCodec());
        if (api != null) {
          channel.setMessageHandler((message, reply) -> {
            HashMap<String, HashMap> wrapped = new HashMap<>();
            try {
              api.startClassicScan();
              wrapped.put("result", null);
            }
            catch (Exception exception) {
              wrapped.put("error", wrapError(exception));
            }
            reply.reply(wrapped);
          });
        } else {
          channel.setMessageHandler(null);
        }
      }
    }
  }

  /** Generated interface from Pigeon that represents a handler of messages from Flutter.*/
  public interface AppLifecycleControl {
    BooleanWrapper waitForBoot();

    /** Sets up an instance of `AppLifecycleControl` to handle messages through the `binaryMessenger` */
    static void setup(BinaryMessenger binaryMessenger, AppLifecycleControl api) {
      {
        BasicMessageChannel<Object> channel =
            new BasicMessageChannel<>(binaryMessenger, "dev.flutter.pigeon.AppLifecycleControl.waitForBoot", new StandardMessageCodec());
        if (api != null) {
          channel.setMessageHandler((message, reply) -> {
            HashMap<String, HashMap> wrapped = new HashMap<>();
            try {
              BooleanWrapper output = api.waitForBoot();
              wrapped.put("result", output.toMap());
            }
            catch (Exception exception) {
              wrapped.put("error", wrapError(exception));
            }
            reply.reply(wrapped);
          });
        } else {
          channel.setMessageHandler(null);
        }
      }
    }
  }

  /** Generated interface from Pigeon that represents a handler of messages from Flutter.*/
  public interface TimelineControl {
    NumberWrapper addPin(TimelinePinPigeon arg);
    NumberWrapper removePin(StringWrapper arg);
    NumberWrapper removeAllPins();

    /** Sets up an instance of `TimelineControl` to handle messages through the `binaryMessenger` */
    static void setup(BinaryMessenger binaryMessenger, TimelineControl api) {
      {
        BasicMessageChannel<Object> channel =
            new BasicMessageChannel<>(binaryMessenger, "dev.flutter.pigeon.TimelineControl.addPin", new StandardMessageCodec());
        if (api != null) {
          channel.setMessageHandler((message, reply) -> {
            HashMap<String, HashMap> wrapped = new HashMap<>();
            try {
              @SuppressWarnings("ConstantConditions")
              TimelinePinPigeon input = TimelinePinPigeon.fromMap((HashMap)message);
              NumberWrapper output = api.addPin(input);
              wrapped.put("result", output.toMap());
            }
            catch (Exception exception) {
              wrapped.put("error", wrapError(exception));
            }
            reply.reply(wrapped);
          });
        } else {
          channel.setMessageHandler(null);
        }
      }
      {
        BasicMessageChannel<Object> channel =
            new BasicMessageChannel<>(binaryMessenger, "dev.flutter.pigeon.TimelineControl.removePin", new StandardMessageCodec());
        if (api != null) {
          channel.setMessageHandler((message, reply) -> {
            HashMap<String, HashMap> wrapped = new HashMap<>();
            try {
              @SuppressWarnings("ConstantConditions")
              StringWrapper input = StringWrapper.fromMap((HashMap)message);
              NumberWrapper output = api.removePin(input);
              wrapped.put("result", output.toMap());
            }
            catch (Exception exception) {
              wrapped.put("error", wrapError(exception));
            }
            reply.reply(wrapped);
          });
        } else {
          channel.setMessageHandler(null);
        }
      }
      {
        BasicMessageChannel<Object> channel =
            new BasicMessageChannel<>(binaryMessenger, "dev.flutter.pigeon.TimelineControl.removeAllPins", new StandardMessageCodec());
        if (api != null) {
          channel.setMessageHandler((message, reply) -> {
            HashMap<String, HashMap> wrapped = new HashMap<>();
            try {
              NumberWrapper output = api.removeAllPins();
              wrapped.put("result", output.toMap());
            }
            catch (Exception exception) {
              wrapped.put("error", wrapError(exception));
            }
            reply.reply(wrapped);
          });
        } else {
          channel.setMessageHandler(null);
        }
      }
    }
  }

  /** Generated interface from Pigeon that represents a handler of messages from Flutter.*/
  public interface KeepUnusedHack {
    void keepPebbleScanDevicePigeon(PebbleScanDevicePigeon arg);

    /** Sets up an instance of `KeepUnusedHack` to handle messages through the `binaryMessenger` */
    static void setup(BinaryMessenger binaryMessenger, KeepUnusedHack api) {
      {
        BasicMessageChannel<Object> channel =
            new BasicMessageChannel<>(binaryMessenger, "dev.flutter.pigeon.KeepUnusedHack.keepPebbleScanDevicePigeon", new StandardMessageCodec());
        if (api != null) {
          channel.setMessageHandler((message, reply) -> {
            HashMap<String, HashMap> wrapped = new HashMap<>();
            try {
              @SuppressWarnings("ConstantConditions")
              PebbleScanDevicePigeon input = PebbleScanDevicePigeon.fromMap((HashMap)message);
              api.keepPebbleScanDevicePigeon(input);
              wrapped.put("result", null);
            }
            catch (Exception exception) {
              wrapped.put("error", wrapError(exception));
            }
            reply.reply(wrapped);
          });
        } else {
          channel.setMessageHandler(null);
        }
      }
    }
  }

  /** Generated interface from Pigeon that represents a handler of messages from Flutter.*/
  public interface ConnectionControl {
    BooleanWrapper isConnected();
    void connectToWatch(NumberWrapper arg);
    void disconnect();
    void sendRawPacket(ListWrapper arg);
    void observeConnectionChanges();
    void cancelObservingConnectionChanges();

    /** Sets up an instance of `ConnectionControl` to handle messages through the `binaryMessenger` */
    static void setup(BinaryMessenger binaryMessenger, ConnectionControl api) {
      {
        BasicMessageChannel<Object> channel =
            new BasicMessageChannel<>(binaryMessenger, "dev.flutter.pigeon.ConnectionControl.isConnected", new StandardMessageCodec());
        if (api != null) {
          channel.setMessageHandler((message, reply) -> {
            HashMap<String, HashMap> wrapped = new HashMap<>();
            try {
              BooleanWrapper output = api.isConnected();
              wrapped.put("result", output.toMap());
            }
            catch (Exception exception) {
              wrapped.put("error", wrapError(exception));
            }
            reply.reply(wrapped);
          });
        } else {
          channel.setMessageHandler(null);
        }
      }
      {
        BasicMessageChannel<Object> channel =
            new BasicMessageChannel<>(binaryMessenger, "dev.flutter.pigeon.ConnectionControl.connectToWatch", new StandardMessageCodec());
        if (api != null) {
          channel.setMessageHandler((message, reply) -> {
            HashMap<String, HashMap> wrapped = new HashMap<>();
            try {
              @SuppressWarnings("ConstantConditions")
              NumberWrapper input = NumberWrapper.fromMap((HashMap)message);
              api.connectToWatch(input);
              wrapped.put("result", null);
            }
            catch (Exception exception) {
              wrapped.put("error", wrapError(exception));
            }
            reply.reply(wrapped);
          });
        } else {
          channel.setMessageHandler(null);
        }
      }
      {
        BasicMessageChannel<Object> channel =
            new BasicMessageChannel<>(binaryMessenger, "dev.flutter.pigeon.ConnectionControl.disconnect", new StandardMessageCodec());
        if (api != null) {
          channel.setMessageHandler((message, reply) -> {
            HashMap<String, HashMap> wrapped = new HashMap<>();
            try {
              api.disconnect();
              wrapped.put("result", null);
            }
            catch (Exception exception) {
              wrapped.put("error", wrapError(exception));
            }
            reply.reply(wrapped);
          });
        } else {
          channel.setMessageHandler(null);
        }
      }
      {
        BasicMessageChannel<Object> channel =
            new BasicMessageChannel<>(binaryMessenger, "dev.flutter.pigeon.ConnectionControl.sendRawPacket", new StandardMessageCodec());
        if (api != null) {
          channel.setMessageHandler((message, reply) -> {
            HashMap<String, HashMap> wrapped = new HashMap<>();
            try {
              @SuppressWarnings("ConstantConditions")
              ListWrapper input = ListWrapper.fromMap((HashMap)message);
              api.sendRawPacket(input);
              wrapped.put("result", null);
            }
            catch (Exception exception) {
              wrapped.put("error", wrapError(exception));
            }
            reply.reply(wrapped);
          });
        } else {
          channel.setMessageHandler(null);
        }
      }
      {
        BasicMessageChannel<Object> channel =
            new BasicMessageChannel<>(binaryMessenger, "dev.flutter.pigeon.ConnectionControl.observeConnectionChanges", new StandardMessageCodec());
        if (api != null) {
          channel.setMessageHandler((message, reply) -> {
            HashMap<String, HashMap> wrapped = new HashMap<>();
            try {
              api.observeConnectionChanges();
              wrapped.put("result", null);
            }
            catch (Exception exception) {
              wrapped.put("error", wrapError(exception));
            }
            reply.reply(wrapped);
          });
        } else {
          channel.setMessageHandler(null);
        }
      }
      {
        BasicMessageChannel<Object> channel =
            new BasicMessageChannel<>(binaryMessenger, "dev.flutter.pigeon.ConnectionControl.cancelObservingConnectionChanges", new StandardMessageCodec());
        if (api != null) {
          channel.setMessageHandler((message, reply) -> {
            HashMap<String, HashMap> wrapped = new HashMap<>();
            try {
              api.cancelObservingConnectionChanges();
              wrapped.put("result", null);
            }
            catch (Exception exception) {
              wrapped.put("error", wrapError(exception));
            }
            reply.reply(wrapped);
          });
        } else {
          channel.setMessageHandler(null);
        }
      }
    }
  }

  /** Generated interface from Pigeon that represents a handler of messages from Flutter.*/
  public interface NotificationsControl {
    void sendTestNotification();

    /** Sets up an instance of `NotificationsControl` to handle messages through the `binaryMessenger` */
    static void setup(BinaryMessenger binaryMessenger, NotificationsControl api) {
      {
        BasicMessageChannel<Object> channel =
            new BasicMessageChannel<>(binaryMessenger, "dev.flutter.pigeon.NotificationsControl.sendTestNotification", new StandardMessageCodec());
        if (api != null) {
          channel.setMessageHandler((message, reply) -> {
            HashMap<String, HashMap> wrapped = new HashMap<>();
            try {
              api.sendTestNotification();
              wrapped.put("result", null);
            }
            catch (Exception exception) {
              wrapped.put("error", wrapError(exception));
            }
            reply.reply(wrapped);
          });
        } else {
          channel.setMessageHandler(null);
        }
      }
    }
  }

  /** Generated class from Pigeon that represents Flutter messages that can be called from Java.*/
  public static class PairCallbacks {
    private final BinaryMessenger binaryMessenger;
    public PairCallbacks(BinaryMessenger argBinaryMessenger){
      this.binaryMessenger = argBinaryMessenger;
    }
    public interface Reply<T> {
      void reply(T reply);
    }
    public void onWatchPairComplete(NumberWrapper argInput, Reply<Void> callback) {
      BasicMessageChannel<Object> channel =
          new BasicMessageChannel<>(binaryMessenger, "dev.flutter.pigeon.PairCallbacks.onWatchPairComplete", new StandardMessageCodec());
      HashMap inputMap = argInput.toMap();
      channel.send(inputMap, channelReply -> {
        callback.reply(null);
      });
    }
  }

  /** Generated class from Pigeon that represents Flutter messages that can be called from Java.*/
  public static class ScanCallbacks {
    private final BinaryMessenger binaryMessenger;
    public ScanCallbacks(BinaryMessenger argBinaryMessenger){
      this.binaryMessenger = argBinaryMessenger;
    }
    public interface Reply<T> {
      void reply(T reply);
    }
    public void onScanUpdate(ListWrapper argInput, Reply<Void> callback) {
      BasicMessageChannel<Object> channel =
          new BasicMessageChannel<>(binaryMessenger, "dev.flutter.pigeon.ScanCallbacks.onScanUpdate", new StandardMessageCodec());
      HashMap inputMap = argInput.toMap();
      channel.send(inputMap, channelReply -> {
        callback.reply(null);
      });
    }
    public void onScanStarted(Reply<Void> callback) {
      BasicMessageChannel<Object> channel =
          new BasicMessageChannel<>(binaryMessenger, "dev.flutter.pigeon.ScanCallbacks.onScanStarted", new StandardMessageCodec());
      channel.send(null, channelReply -> {
        callback.reply(null);
      });
    }
    public void onScanStopped(Reply<Void> callback) {
      BasicMessageChannel<Object> channel =
          new BasicMessageChannel<>(binaryMessenger, "dev.flutter.pigeon.ScanCallbacks.onScanStopped", new StandardMessageCodec());
      channel.send(null, channelReply -> {
        callback.reply(null);
      });
    }
  }

  /** Generated class from Pigeon that represents Flutter messages that can be called from Java.*/
  public static class ConnectionCallbacks {
    private final BinaryMessenger binaryMessenger;
    public ConnectionCallbacks(BinaryMessenger argBinaryMessenger){
      this.binaryMessenger = argBinaryMessenger;
    }
    public interface Reply<T> {
      void reply(T reply);
    }
    public void onWatchConnectionStateChanged(WatchConnectionStatePigeon argInput, Reply<Void> callback) {
      BasicMessageChannel<Object> channel =
          new BasicMessageChannel<>(binaryMessenger, "dev.flutter.pigeon.ConnectionCallbacks.onWatchConnectionStateChanged", new StandardMessageCodec());
      HashMap inputMap = argInput.toMap();
      channel.send(inputMap, channelReply -> {
        callback.reply(null);
      });
    }
  }

  /** Generated interface from Pigeon that represents a handler of messages from Flutter.*/
  public interface DebugControl {
    void collectLogs();

    /** Sets up an instance of `DebugControl` to handle messages through the `binaryMessenger` */
    static void setup(BinaryMessenger binaryMessenger, DebugControl api) {
      {
        BasicMessageChannel<Object> channel =
            new BasicMessageChannel<>(binaryMessenger, "dev.flutter.pigeon.DebugControl.collectLogs", new StandardMessageCodec());
        if (api != null) {
          channel.setMessageHandler((message, reply) -> {
            HashMap<String, HashMap> wrapped = new HashMap<>();
            try {
              api.collectLogs();
              wrapped.put("result", null);
            }
            catch (Exception exception) {
              wrapped.put("error", wrapError(exception));
            }
            reply.reply(wrapped);
          });
        } else {
          channel.setMessageHandler(null);
        }
      }
    }
  }
  private static HashMap wrapError(Exception exception) {
    HashMap<String, Object> errorMap = new HashMap<>();
    errorMap.put("message", exception.toString());
    errorMap.put("code", exception.getClass().getSimpleName());
    errorMap.put("details", null);
    return errorMap;
  }
}
