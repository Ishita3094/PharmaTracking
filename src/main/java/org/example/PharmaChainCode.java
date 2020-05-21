package org.example;

import Model.MedicineBatch;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.uuid.Generators;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.List;

public class PharmaChainCode extends ChaincodeBase {
    private class ChaincodeResponse {
        public String message;
        public String code;
        public boolean OK;

        public ChaincodeResponse(String message, String code, boolean OK) {
            this.code = code;
            this.message = message;
            this.OK = OK;
        }
    }

    @Override
    public Response init(ChaincodeStub stub) {
        return null;
    }

    @Override
    public Response invoke(ChaincodeStub stub) {
        return null;
    }

    private String responseError(String errorMessage, String code) {
        try {
            return (new ObjectMapper()).writeValueAsString(new ChaincodeResponse(errorMessage, code, false));
        } catch (Throwable e) {
            return "{\"code\":'" + code + "', \"message\":'" + e.getMessage() + " AND " + errorMessage + "', \"OK\":" + false + "}";
        }
    }

    private String responseSuccess(String successMessage) {
        try {
            return (new ObjectMapper()).writeValueAsString(new ChaincodeResponse(successMessage, "", true));
        } catch (Throwable e) {
            return "{\"message\":'" + e.getMessage() + " BUT " + successMessage + " (NO COMMIT)', \"OK\":" + false + "}";
        }
    }

    private boolean checkString(String str) {
        if (str.trim().length() <= 0 || str == null)
            return false;
        return true;
    }

    private String responseSuccessObject(String object) {
        return "{\"message\":" + object + ", \"OK\":" + true + "}";
    }

    public Response createBatch(ChaincodeStub stub, List<String> args){
        if (args.size() != 4)
            return newErrorResponse(responseError("Incorrect number of arguments, expecting 4", ""));
        String batchId = Generators.timeBasedGenerator().generate().toString();
        String name = args.get(1);
        String price = args.get(2);
        int quantity = Integer.parseInt(args.get(3));

        double doublePrice = 0.0;
        try{
        doublePrice = Double.parseDouble(price);
            if(doublePrice < 0.0)
                return newErrorResponse(responseError("Invalid price", ""));
        } catch (NumberFormatException e) {
            return newErrorResponse(responseError("parseInt error", ""));
        }
        MedicineBatch batch = new MedicineBatch(batchId, name, doublePrice, quantity);

        try {
            if (checkString(stub.getStringState(batchId)))
                return newErrorResponse(responseError("Existing batchId", ""));
            stub.putState(batchId, (new ObjectMapper()).writeValueAsBytes(batch));
            return newSuccessResponse(responseSuccess("Batch created"));
        }catch (Throwable e) {
            return newErrorResponse(responseError(e.getMessage(), ""));
        }
    }

    private Response getBatch(ChaincodeStub stub, List<String> args) {
        if (args.size() != 1)
            return newErrorResponse(responseError("Incorrect number of arguments, expecting 1", ""));
        String batchId = args.get(0);
        if (!checkString(batchId))
            return newErrorResponse(responseError("Invalid argument", ""));
        try {
            String batch = stub.getStringState(batchId);
            if(!checkString(batch))
                return newErrorResponse(responseError("Nonexisting batch", ""));
            return newSuccessResponse((new ObjectMapper()).writeValueAsBytes(responseSuccessObject(batch)));
        } catch(Throwable e){
            return newErrorResponse(responseError(e.getMessage(), ""));
        }
    }

    private Response transfer(ChaincodeStub stub, List<String> args) {
        if (args.size() != 6)
            return newErrorResponse(responseError("Incorrect number of arguments, expecting 6", ""));
        String fromPeerId = args.get(0);
        String toPeerId = args.get(1);
        String batchId = args.get(2);
        String name = args.get(3);
        String price = args.get(4);
        String quantity = args.get(5);

        if (!checkString(fromPeerId) || !checkString(toPeerId) || !checkString(batchId) || !checkString(quantity))
            return newErrorResponse(responseError("Invalid argument(s)", ""));
        if(fromPeerId.equals(toPeerId))
            return newErrorResponse(responseError("From-peer is same as to-peer", ""));

        try {
            String fromPeerString = stub.getStringState(fromPeerId);
            if(!checkString(fromPeerString))
                return newErrorResponse(responseError("Nonexistent from-Peer", ""));
            String toPeerString = stub.getStringState(toPeerId);
            if(!checkString(toPeerString))
                return newErrorResponse(responseError("Nonexistent to-Peer", ""));

            ObjectMapper objectMapper = new ObjectMapper();
            MedicineBatch fromPeer = objectMapper.readValue(fromPeerString, MedicineBatch.class);
            MedicineBatch toPeer = objectMapper.readValue(toPeerString, MedicineBatch.class);

            toPeer.setBatchId(fromPeer.getBatchId());
            toPeer.setName(fromPeer.getName());
            toPeer.setPrice(fromPeer.getPrice());
            toPeer.setQuantity(fromPeer.getQuantity());

            return newSuccessResponse(responseSuccess("Transferred"));
        } catch(Throwable e){
            return newErrorResponse(responseError(e.getMessage(), ""));
        }
    }

    public static void main(String[] args) {
        new PharmaChainCode().start(args);
    }
}
