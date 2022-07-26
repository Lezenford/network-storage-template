package ru.gb.storage.commons.message;


import java.util.Date;

public class DataMessage extends Message{

    private Date data;

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "DataMessage{" +
                "data=" + data +
                '}';
    }
}
