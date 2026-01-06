
#include <Wire.h>
#include "DHT.h"
#include <math.h>
#include <SoftwareSerial.h>

int BH1750address = 0x23;
const int rxPin = 11; // 这个引脚需要连接到传感器的TX
const int txPin = 10; // 这个引脚需要连接到传感器的RX

SoftwareSerial co2Sensor(rxPin, txPin);
DHT dht(2,DHT11);
byte buff[2];
int co2;  
bool co2DataValid = false;
int lastValidCO2 = 400; // 保存上次有效的CO2读数

void setup()
{
    Wire.begin();
    Serial.begin(9600);   
    dht.begin();      
    co2Sensor.begin(9600);
    pinMode(A0,INPUT);
    delay(1000);
    
    // Serial.println("JW01 CO2传感器多方法容错系统启动");
    delay(500);
}
 
void loop()
{
    float oc = analogRead(A0);
    float humd = dht.readHumidity();               
    float temp = dht.readTemperature(); 
    uint16_t val = 0;
    BH1750_Init(BH1750address);
    delay(200);
    if (2 == BH1750_Read(BH1750address)) {
        val = ((buff[0] << 8) | buff[1]) / 1.2;
    }
    
    co2 = readCO2_MultiMethod();

    if (co2DataValid) {
        lastValidCO2 = co2; 
        Serial.println(String(val)+";"+String(temp)+";"+String(humd)+";"+String(oc/42.5*1.875)+";"+String(co2));
    } else {

        Serial.println(String(val)+";"+String(temp)+";"+String(humd)+";"+String(oc/42.5*1.875)+";"+String(lastValidCO2));
    }
    
    delay(5000);
}

int readCO2_MultiMethod() {
    int result = -1;
    co2DataValid = false;
    
    // 方法1：发送完整命令
    result = readCO2_Method1();
    if (co2DataValid) {
        // Serial.println("方法1成功");
        return result;
    }
    
    // 方法2：直接读取（不发送命令）
    result = readCO2_Method2();
    if (co2DataValid) {
        // Serial.println("方法2成功");
        return result;
    }
    
    result = readCO2_Method3();
    if (co2DataValid) {
        // Serial.println("方法3成功");
        return result;
    }
  
    result = readCO2_Method4();
    if (co2DataValid) {
        // Serial.println("方法4成功");
        return result;
    }
    
    // Serial.println("所有方法都失败");
    return -1;
}

int readCO2_Method1() {
    co2DataValid = false;
    while(co2Sensor.available()) {
        co2Sensor.read();
    }
    
    byte command[9] = {0xFF, 0x01, 0x86, 0x00, 0x00, 0x00, 0x00, 0x00, 0x79};
    co2Sensor.write(command, 9);
    co2Sensor.flush();
    
    delay(300); 
    
    if (!co2Sensor.available()) {
        return -1;
    }
    
    return parseDataPacket();
}

int readCO2_Method2() {
    co2DataValid = false;

    delay(500); 
    
    if (!co2Sensor.available()) {
        return -1;
    }
    
    return parseDataPacket();
}

int readCO2_Method3() {
    co2DataValid = false;
    while(co2Sensor.available()) {
        co2Sensor.read();
    }
    
    byte command[3] = {0xFF, 0x01, 0x86};
    co2Sensor.write(command, 3);
    co2Sensor.flush();
    
    delay(400);
    
    if (!co2Sensor.available()) {
        return -1;
    }
    
    return parseDataPacket();
}
int readCO2_Method4() {
    co2DataValid = false;
    while(co2Sensor.available()) {
        co2Sensor.read();
    }
    
    byte command[9] = {0xFF, 0x01, 0x86, 0x00, 0x00, 0x00, 0x00, 0x00, 0x79};
    co2Sensor.write(command, 9);
    co2Sensor.flush();
    
    delay(800); 
    
    if (!co2Sensor.available()) {
        return -1;
    }
    
    return parseDataPacket();
}

int parseDataPacket() {
    byte packet[10]; 
    int bytesRead = 0;
    unsigned long startTime = millis();
    while (co2Sensor.available() && bytesRead < 10 && (millis() - startTime) < 1000) {
        packet[bytesRead] = co2Sensor.read();
        bytesRead++;
        delay(5); 
    }
    if (bytesRead < 3) {
        return -1;
    }
    
    // 查找我们知道有效的数据模式：0x2C开头
    for (int i = 0; i <= bytesRead - 3; i++) {
        if (packet[i] == 0x2C && (i + 2) < bytesRead) {
            int co2Value = (packet[i+1] << 8) | packet[i+2];
          
            if (co2Value >= 250 && co2Value <= 5000) {
                co2DataValid = true;
                return co2Value;
            }
        }
    }
    
    for (int i = 0; i < bytesRead - 1; i++) {
        int co2Value = (packet[i] << 8) | packet[i+1];
        if (co2Value >= 250 && co2Value <= 5000) {
            co2DataValid = true;
            return co2Value;
        }
        
        co2Value = packet[i] | (packet[i+1] << 8);
        if (co2Value >= 250 && co2Value <= 5000) {
            co2DataValid = true;
            return co2Value;
        }
    }
    
    return -1;
}

int BH1750_Read(int address) {
    int i = 0;
    Wire.beginTransmission(address);
    Wire.requestFrom(address, 2);
    while (Wire.available()) {
        buff[i] = Wire.read();
        i++;
    }
    Wire.endTransmission();
    return i;
}
 
void BH1750_Init(int address) {
    Wire.beginTransmission(address);
    Wire.write(0x10);
    Wire.endTransmission();
}
