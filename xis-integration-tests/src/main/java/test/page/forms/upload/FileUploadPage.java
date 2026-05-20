package test.page.forms.upload;

import one.xis.Action;
import one.xis.FormData;
import one.xis.ModelData;
import one.xis.Page;

@Page("/file-upload.html")
class FileUploadPage {

    @FormData("upload")
    FileUploadForm upload() {
        return new FileUploadForm();
    }

    @Action("save")
    @ModelData("result")
    @FormData("upload")
    String save(@FormData("upload") FileUploadForm form) {
        return form.getDescription() + ":" + form.getAttachment().getFileName() + ":" + form.getAttachment().getUtf8Text();
    }
}
