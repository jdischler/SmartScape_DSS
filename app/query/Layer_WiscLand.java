package query;

// FIXME: TODO: This thing is kind of weird. I mean, it returns an entirely unrelated type...
//	What it was *meant* to do was to simplify getting the wisc land layer to some extent...
//	but more importantly, also be a helpful holding place for enums that would add some compile-time
//	checking for getting masks vs. just using the strings (which have no compile-time checks)
// One obvious downside (among many) is the "wisc" part, which isn't general/portable.
//------------------------------------------------------------------------------
public class Layer_WiscLand
{
	public static Layer_Integer get() {
		return (Layer_Integer)Layer_Base.getLayer("wisc_land"); 
	}
}

