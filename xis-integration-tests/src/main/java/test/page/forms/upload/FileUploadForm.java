package test.page.forms.upload;

import one.xis.Upload;
import one.xis.UploadedFile;

class FileUploadForm {
    private String description;

    @Upload(maxSize = 20)
    private UploadedFile attachment;

    @Upload
    private UploadedFile secondAttachment;

    public String getDescription() {
        return description;
    }

    public UploadedFile getAttachment() {
        return attachment;
    }

    public UploadedFile getSecondAttachment() {
        return secondAttachment;
    }
}
