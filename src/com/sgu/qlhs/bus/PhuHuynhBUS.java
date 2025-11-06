package com.sgu.qlhs.bus;

import com.sgu.qlhs.database.PhuHuynhDAO;
import com.sgu.qlhs.dto.PhuHuynhDTO;
import java.util.List;

public class PhuHuynhBUS {
    private PhuHuynhDAO dao = new PhuHuynhDAO();

    public List<PhuHuynhDTO> getByHocSinh(int maHS) {
        return dao.getByHocSinh(maHS);
    }
}
