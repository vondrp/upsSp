package vondrovic.ups.sp.client;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Class I18Support is class used for language support
 */
public class I18Support {


    private final ResourceBundle bundle = ResourceBundle.getBundle("lang/messages");

    /**
     *
     * @param key       key string
     * @param params    parameters of the key
     * @return          translated meaning of the key
     */
    public String getString(String key, Object... params)
    {
        try
        {
            String value = bundle.getString(key);

            if(params.length > 0)
            {
                return MessageFormat.format(value, params);
            }

            return value;
        }
        catch (MissingResourceException e)
        {
            return "! " + key + " !";
        }
    }

    /**
     *
     * @return  ResourceBundle of the application language
     */
    public ResourceBundle getResourceBundle()
    {
        return this.bundle;
    }
}
