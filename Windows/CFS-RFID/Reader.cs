using System;
using PCSC;
using PCSC.Iso7816;

namespace CFS_RFID
{
    public class Reader
    {
        private const byte CUSTOM_CLA = 0xFF;
        private readonly IIsoReader _isoReader;

        public Reader(IIsoReader isoReader)
        {
            _isoReader = isoReader ?? throw new ArgumentNullException(nameof(isoReader));
        }

        public byte[] GetData()
        {
            var getDataCmd = new CommandApdu(IsoCase.Case2Short, SCardProtocol.Any)
            {
                CLA = CUSTOM_CLA,
                Instruction = InstructionCode.GetData,
                P1 = 0x00,
                P2 = 0x00
            };

            var response = _isoReader.Transmit(getDataCmd);
            return IsSuccess(response)
                    ? response.GetData() ?? new byte[0]
                    : null;
        }

        public bool LoadKey(byte keyStructure, byte keyNumber, byte[] key)
        {
            var loadKeyCmd = new CommandApdu(IsoCase.Case3Short, SCardProtocol.Any)
            {
                CLA = CUSTOM_CLA,
                Instruction = InstructionCode.ExternalAuthenticate,
                P1 = (byte)keyStructure,
                P2 = keyNumber,
                Data = key
            };
            var response = _isoReader.Transmit(loadKeyCmd);
            return IsSuccess(response);
        }


        public bool Authenticate(byte msb, byte lsb, byte keyType, byte keyNumber)
        {
            var authBlock = new Auth
            {
                Msb = msb,
                Lsb = lsb,
                KeyNumber = keyNumber,
                KeyType = keyType
            };
            var authKeyCmd = new CommandApdu(IsoCase.Case3Short, SCardProtocol.Any)
            {
                CLA = CUSTOM_CLA,
                Instruction = InstructionCode.InternalAuthenticate,
                P1 = 0x00,
                P2 = 0x00,
                Data = authBlock.ToArray()
            };
            var response = _isoReader.Transmit(authKeyCmd);
            return (response.SW1 == 0x90) && (response.SW2 == 0x00);
        }

        public byte[] ReadBinary(byte msb, byte lsb, int size)
        {
            unchecked
            {
                var readBinaryCmd = new CommandApdu(IsoCase.Case2Short, SCardProtocol.Any)
                {
                    CLA = CUSTOM_CLA,
                    Instruction = InstructionCode.ReadBinary,
                    P1 = msb,
                    P2 = lsb,
                    Le = size
                };
                var response = _isoReader.Transmit(readBinaryCmd);
                return IsSuccess(response)
                    ? response.GetData() ?? new byte[0]
                    : null;
            }
        }

        public bool UpdateBinary(byte msb, byte lsb, byte[] data)
        {
            var updateBinaryCmd = new CommandApdu(IsoCase.Case3Short, SCardProtocol.Any)
            {
                CLA = CUSTOM_CLA,
                Instruction = InstructionCode.UpdateBinary,
                P1 = msb,
                P2 = lsb,
                Data = data
            };
            var response = _isoReader.Transmit(updateBinaryCmd);
            return IsSuccess(response);
        }

        public bool Decrement(byte msb, byte lsb, byte[] data)
        {
            var decrementCmd = new CommandApdu(IsoCase.Case3Short, SCardProtocol.Any)
            {
                CLA = CUSTOM_CLA,
                Instruction = InstructionCode.Decrement,
                P1 = 0x00,
                P2 = lsb,
                Data = data,
            };
            var response = _isoReader.Transmit(decrementCmd);
            return IsSuccess(response);
        }


        public bool Increment(byte msb, byte lsb, byte[] data)
        {
            var incrementCmd = new CommandApdu(IsoCase.Case3Short, SCardProtocol.Any)
            {
                CLA = CUSTOM_CLA,
                Instruction = InstructionCode.Increment,
                P1 = 0x00,
                P2 = lsb,
                Data = data
            };
            var response = _isoReader.Transmit(incrementCmd);
            return IsSuccess(response);
        }

        private static bool IsSuccess(Response response) =>
            (response.SW1 == (byte)SW1Code.Normal) &&
            (response.SW2 == 0x00);
    }

}