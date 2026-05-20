package one.xis.totp;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import one.xis.context.Component;

@Component
class QRCodeSvgRenderer {

    String render(String text) {
        try {
            var matrix = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 33, 33);
            int size = matrix.getWidth();
            StringBuilder svg = new StringBuilder();
            svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 ")
                    .append(size).append(' ').append(size)
                    .append("\" shape-rendering=\"crispEdges\">");
            svg.append("<rect width=\"100%\" height=\"100%\" fill=\"#fff\"/>");
            svg.append("<path fill=\"#000\" d=\"");
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    if (matrix.get(x, y)) {
                        svg.append('M').append(x).append(' ').append(y).append("h1v1h-1z");
                    }
                }
            }
            svg.append("\"/></svg>");
            return svg.toString();
        } catch (WriterException e) {
            throw new IllegalStateException("Unable to render TOTP QR code", e);
        }
    }
}
