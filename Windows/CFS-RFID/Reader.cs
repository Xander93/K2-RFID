using PCSC;
using System;
using System.Collections.Generic;
using System.Linq;

namespace CFS_RFID
{
    public class Reader
    {
        private readonly ICardReader reader;

        public Reader(ICardReader icReader)
        {
            reader = icReader ?? throw new ArgumentNullException(nameof(icReader));
        }

        public byte[] GetData()
        {
            byte[] response = new byte[6];
            reader.Transmit(new byte[] { 0xFF, 0xCA, 0x00, 0x00, 0x00 }, response);
            Array.Resize(ref response, 4);
            return response;
        }

        public bool LoadAuthenticationKeys(byte keyStructure, byte keyNumber, byte[] key)
        {
            byte[] response = new byte[2];
            List<byte> command = new byte[] { 0xFF, 0x82, keyStructure, keyNumber, 0x06 }.ToList();
            command.AddRange(key);
            reader.Transmit(command.ToArray(), response);
            return (response[0] == 0x90) && (response[1] == 0x00);
        }

        public bool Authentication10byte(byte block, byte keyType, byte keyNumber)
        {
            byte[] response = new byte[2];
            List<byte> command = new byte[] { 0xFF, 0x86, 0x00, 0x00, 0x05 }.ToList();
            command.AddRange(new byte[] { 0x01, 0x00, block, keyType, keyNumber });
            reader.Transmit(command.ToArray(), response);
            return (response[0] == 0x90) && (response[1] == 0x00);
        }

        public bool Authentication6byte(byte block, byte keyType, byte keyNumber)
        {
            byte[] response = new byte[2];
            List<byte> command = new byte[] { 0xFF, 0x88, 0x00, block, keyType }.ToList();
            command.AddRange(new byte[] { keyNumber });
            reader.Transmit(command.ToArray(), response);
            return (response[0] == 0x90) && (response[1] == 0x00);
        }

        public byte[] ReadBinaryBlocks(int block, int len)
        {
            byte[] response = new byte[len + 2];
            reader.Transmit(new byte[] { 0xFF, 0xB0, 0x00, (byte)block, (byte)len }, response);
            Array.Resize(ref response, len);
            return response;
        }

        public bool UpdateBinaryBlocks(int block, int len, byte[] blockData)
        {
            byte[] response = new byte[2];
            List<byte> command = new byte[] { 0xFF, 0xD6, 0x00, (byte)block, (byte)len }.ToList();
            command.AddRange(blockData);
            reader.Transmit(command.ToArray(), response);
            return (response[0] == 0x90) && (response[1] == 0x00);
        }

        public byte[] GetFirmwareVersion()
        {
            byte[] response = new byte[12];
            reader.Transmit(new byte[] { 0xFF, 0x00, 0x48, 0x00, 0x00 }, response);
            Array.Resize(ref response, 10);
            return response;
        }

        public bool SetBuzzerOutputduringCardDetection(bool on)
        {
            byte[] response = new byte[2];
            reader.Transmit(new byte[] { 0xFF, 0x00, 0x52, (byte)(on ? 0xff : 0x00), 0x00 }, response);
            return (response[0] == 0x90) && (response[1] == 0x00);
        }
    }
}