import sys,unittest,hmac,hashlib
from pathlib import Path
sys.path.insert(0,str(Path(__file__).resolve().parents[1]/'tools'))
from box_client import request,verify_state,may_start_travel

KEY=bytes(range(32));NONCE='66dc9042ce028dcfb1abb4e541656179'
PAYLOAD='S 1 0 1 0 1 1 0 0 1 1 4990 1 1 11800 1'
def sign(payload=PAYLOAD,nonce=NONCE):return payload+' '+hmac.new(KEY,f'R3-RESP|{nonce}|{payload}'.encode(),hashlib.sha256).hexdigest()

class ProtocolTests(unittest.TestCase):
    def test_cpp_request_vector(self):
        n,a=request(KEY,'STATUS','CHALLENGE STATUS '+NONCE)
        self.assertEqual(n,NONCE)
        self.assertEqual(a,'AUTH e6fe1e40630b91a832182f1c6ca94adc2bae7211ae0bc44e4a849125b9abc7d0')
    def test_cpp_response_vector(self):
        self.assertTrue(sign().endswith('00b96e045945a8557568f0da1e5ab7772da57a7c5dcc512bc7b53c54e2668595'))
        self.assertTrue(may_start_travel(verify_state(KEY,NONCE,sign(),.2)))
    def test_verb_substitution(self):
        with self.assertRaises(ValueError):request(KEY,'UNLOCK','CHALLENGE CLOSE '+NONCE)
    def test_changed_state(self):
        with self.assertRaises(ValueError):verify_state(KEY,NONCE,sign().replace('4990','0'),.2)
    def test_replayed_old_nonce(self):
        with self.assertRaises(ValueError):verify_state(KEY,'0'*32,sign(),.2)
    def test_response_freshness(self):
        for elapsed in (-1,2.0,3):
            with self.assertRaises(ValueError):verify_state(KEY,NONCE,sign(),elapsed)
    def test_unauthenticated_state(self):
        with self.assertRaises(ValueError):verify_state(KEY,NONCE,PAYLOAD,.2)
    def test_wrong_key(self):
        with self.assertRaises(ValueError):verify_state(bytes(32),NONCE,sign(),.2)
    def test_signed_but_invalid_boolean(self):
        bad=PAYLOAD.replace('S 1','S 2',1)
        with self.assertRaises(ValueError):verify_state(KEY,NONCE,sign(bad),.2)
    def test_closed_is_not_enough(self):
        state=verify_state(KEY,NONCE,sign(),.2)
        for field,value in [('weight_valid',0),('faults',1),('panel',0),('tray',0),('extended',0),('retracted',1),('stowed',1),('accepted',0),('power_mv',9000),('coil',1)]:
            bad=dict(state);bad[field]=value;self.assertFalse(may_start_travel(bad),field)

if __name__=='__main__':unittest.main(verbosity=2)
