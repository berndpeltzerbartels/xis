package test.page;

interface SimpleFormService {

    SimpleFormObject getById(Integer id);

    void save(SimpleFormObject object);
}
