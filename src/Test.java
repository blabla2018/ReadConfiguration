public class Test {
    public static void main( String[] args ){
        Configuration conf = new Configuration();
        System.out.println(conf.getProperty("sdd", "default value"));
        System.out.println(conf.getLongProperty("selenium.waitForElementTimeout", 10000L).intValue());
        System.out.println(conf.getIntProperty("selenium.waitForElementTimeout", 100));
        System.out.println(conf.getBooleanProperty("test.test", false));
    }
}


