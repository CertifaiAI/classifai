package ai.classifai.core.entity.trait;

/**
 * Every entity interface must implement HasID for database schema
 *
 * @author YinChuangSum
 */
public interface HasId<Id>
{
    public Id getId();
}
