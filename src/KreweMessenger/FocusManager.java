
package KreweMessenger;

public class FocusManager 
{
    private boolean isFocused;
    
    public FocusManager()
    {
        isFocused = true;
    }
    
    public void setFocus(boolean newValue)
    {
        isFocused = newValue;
    }
    
    public boolean getFocus()
    {
        return isFocused;
    }
    
}
